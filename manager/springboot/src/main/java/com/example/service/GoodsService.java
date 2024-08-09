package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.mapper.*;
import com.example.utils.TokenUtils;
import com.example.utils.UserCF;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 商品信息表业务处理
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private CollectMapper collectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private OrdersMapper ordersMapper;

    /**
     * 新增
     */
    public void add(Goods goods) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        goodsMapper.insert(goods);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            goodsMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
    }

    /**
     * 根据ID查询
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * 查询Top15热门商品
     */
    public List<Goods> selectTop15() {
        return goodsMapper.selectTop15();
    }

    public List<Goods> selectByTypeId(Integer id) {
        return goodsMapper.selectByTypeId(id);
    }

    public List<Goods> selectByBusinessId(Integer id) {
        return goodsMapper.selectByBusinessId(id);
    }

    public List<Goods> selectByName(String name) {
        return goodsMapper.selectByName(name);
    }

    /**
     * 用户的 收藏，购物车，下单，评论 行为对商品的推荐产生了关系
     * 通过协同过滤算法，计算商品之间的相似度，得到推荐商品
     **/
    public List<Goods> selectRecommend() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (ObjectUtil.isEmpty(currentUser)) {
            // 如果没有用户登录
            return new ArrayList<>();
        }
        // 1. 获取所有的收藏信息
        List<Collect> allCollect = collectMapper.selectAll(null);
        // 2. 获取所有的购物车信息
        List<Cart> allCart = cartMapper.selectAll(null);
        // 3. 获取所有的下单信息
        List<Orders> allOrders = ordersMapper.selectAllCompleteOrders();
        // 4. 获取所有的评论信息
        List<Comment> allComment = commentMapper.selectAll(null);
        // 5. 获取所有的用户信息
        List<User> allUser = userMapper.selectAll(null);
        // 6. 获取所有的商品信息
        List<Goods> allGoods = goodsMapper.selectAll(null);

        // 定义一个存储每个商品和每个用户之间的关系数据的List
        List<RelateDTO> relateList = new ArrayList<>();
        // 定义一个存储最后返回给前端的商品List
        List<Goods> recommendGoods;

        // 创建一个栅栏，等待所有的异步任务执行完毕
        CountDownLatch countDownLatch = new CountDownLatch(allGoods.size() * allUser.size());
        // 创建一个线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();

        // 计算每个商品和每个用户之间的关系数据
        for (Goods goods : allGoods) {
            Integer goodsId = goods.getId();
            for (User user : allUser) {
                // 通过线程池来执行任务
                threadPool.execute(() -> {
                    Integer userId = user.getId();
                    int index = 1; // 默认关系值为1
                    // 1. 判断该用户有没有收藏该商品(收藏权重: 1)
                    Optional<Collect> collectOptional = allCollect.stream().filter(x -> x.getGoodsId().equals(goodsId)
                            && x.getUserId().equals(userId)).findFirst();
                    if (collectOptional.isPresent()) {
                        index += 1;
                    }
                    // 2. 判断该用户有没有将该商品加入购物车(购物车权重: 2)
                    Optional<Cart> cartOptional = allCart.stream().filter(x -> x.getGoodsId().equals(goodsId)
                            && x.getUserId().equals(userId)).findFirst();
                    if (cartOptional.isPresent()) {
                        index += 2;
                    }
                    // 3. 判断该用户有没有下单该商品(下单权重: 3)
                    Optional<Orders> ordersOptional = allOrders.stream().filter(x -> x.getGoodsId().equals(goodsId)
                            && x.getUserId().equals(userId)).findFirst();
                    if (ordersOptional.isPresent()) {
                        index += 3;
                    }
                    // 4. 判断该用户有没有评论该商品(评论权重: 2)
                    Optional<Comment> commentOptional = allComment.stream().filter(x -> x.getGoodsId().equals(goodsId)
                            && x.getUserId().equals(userId)).findFirst();
                    if (commentOptional.isPresent()) {
                        index += 2;
                    }
                    // 5. 将该关系数据存储到List中
                    if (index > 1) {
                        relateList.add(new RelateDTO(userId, goodsId, index));
                    }
                    countDownLatch.countDown(); // 栅栏减1
                });
            }
        }

        try {
            countDownLatch.await();
            threadPool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 数据准备结束后将数据给协同过滤算法进行计算，得到推荐商品
            List<Integer> goodsIds = UserCF.recommend(currentUser.getId(), relateList);
            // 把推荐商品的id转换为商品对象
            recommendGoods = goodsIds.stream().map(goodsId -> allGoods.stream()
                    .filter(x -> x.getId().equals(goodsId)).findFirst().orElse(null)).limit(10).collect(Collectors.toList());
        }

        if (CollectionUtil.isEmpty(recommendGoods)) {
            // 随机给它推荐10个
            return getRandomGoods(10);
        }
        if (recommendGoods.size() < 10) {
            int num = 10 - recommendGoods.size();
            List<Goods> list = getRandomGoods(num);
            recommendGoods.addAll(list);
        }

        return recommendGoods;
    }

    private List<Goods> getRandomGoods(int num) {
        List<Goods> list = new ArrayList<>(num);
        List<Goods> goods = goodsMapper.selectAll(null);
        for (int i = 0; i < num; i++) {
            int index = new Random().nextInt(goods.size());
            list.add(goods.get(index));
        }
        return list;
    }

    /**
     * 分页查询
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }
}