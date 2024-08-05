<template>
  <div class="main-content">
    <div style="width: 60%; background-color: white; min-height: 1000px; margin: 20px auto; border-radius: 20px">
      <div style="padding: 15px 20px">
        <el-row :gutter="20">
          <el-col :span="12">
            <img :src="goodsData.img" alt="" style="width: 100%; height: 400px; border-radius: 20px;">
          </el-col>
          <el-col :span="12">
            <div
                style="font-size: 20px; font-weight: 900; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">
              {{ goodsData.name }}
            </div>
            <div style="color: #666666FF; margin-top: 5px;">销量：{{ goodsData.count }}</div>
            <div style="color: orange; margin-top: 15px;">疯抢价：<span style="font-size: 20px;">{{ goodsData.price }} / {{
                goodsData.unit
              }}</span></div>
            <div style="margin-top: 20px;">
              <img src="@/assets/imgs/right.png" alt="" style="width: 70%; height: 130px; border-radius: 15px;">
            </div>
            <div style="color: #666666FF; margin-top: 20px;">商家：<a href="#"
                                                                     @click="navTo('/front/business?id=' + goodsData.businessId)">
              {{ goodsData.businessName }} </a></div>
            <div style="color: #666666FF; margin-top: 20px;">分类：<a href="#"
                                                                     @click="navTo('/front/type?id=' + goodsData.typeId)">
              {{ goodsData.typeName }} </a></div>
            <div style="color: #666666FF; margin-top: 20px;">
              <el-button type="warning">加入购物车</el-button>
              <el-button type="warning" @click="collectGoods()">收藏商品</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
      <div style="margin-top: 30px; padding: 15px 20px;">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="商品详情" name="first">
            <div style="padding: 10px 175px" v-html="goodsData.description"></div>
          </el-tab-pane>
          <el-tab-pane label="商品评价" name="second">非常好东西 使我的钱包瘪瘪</el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>

import goods from "../manager/Goods.vue";

export default {
  computed: {
    goods() {
      return goods
    }
  },
  data() {
    let goodsId = this.$route.query.id
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      goodsId: goodsId,
      goodsData: {},
      activeName: 'first'
    }
  },
  mounted() {
    this.loadGoods()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadGoods() {
      this.$request.get('goods/selectById?id=' + this.goodsId).then(res => {
        if (res.code === '200') {
          this.goodsData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleClick(tab, event) {
      this.activeName = tab.name
    },
    navTo(url) {
      location.href = url
    },
    collectGoods() {
      let data = {userId: this.user.id, goodsId: this.goodsId, businessId: this.goodsData.businessId}
      this.$request.post('collect/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('收藏成功')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>