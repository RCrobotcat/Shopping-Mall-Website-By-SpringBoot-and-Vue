<template>
  <div class="main-content">
    <div
        style="width: 70%; background-color: white; margin: 30px auto; border-radius: 20px;">
      <div style="padding-bottom: 10px;">
        <div style="display: flex; font-size: 18px; color: black; line-height: 80px; border-bottom: #cccccc 1px solid;">
          <div style="margin-left: 20px; flex: 1;">购物车（共 {{ goodsData.length }} 件）</div>
          <div style="flex: 2; text-align: right;">
            <el-select v-model="addressId" placeholder="请选择收货地址" style="width: 70%;">
              <el-option v-for="item in addressData"
                         :label="item.username + ' - ' + item.useraddress + ' - ' + item.phone"
                         :value="item.id"></el-option>
            </el-select>
          </div>
          <div style="flex: 1; font-size: 16px; text-align: right; padding-right: 20px;">
            已选商品 <span style="color: coral; font-size: 20px;">¥ {{ totalPrice }}</span>
            <el-button type="warning" round style="margin-left: 10px;" @click="pay()">下单</el-button>
          </div>
        </div>
        <div style="margin: 20px 0; padding: 0 50px;">
          <div class="table">
            <el-table :data="goodsData" strip @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" align="center"></el-table-column>
              <el-table-column label="商品图片" width="120px">
                <template v-slot="scope">
                  <div style="display: flex; align-items: center">
                    <el-image style="width: 80px; height: 60px; border-radius: 3px" v-if="scope.row.goodsImg"
                              :src="scope.row.goodsImg" :preview-src-list="[scope.row.goodsImg]"></el-image>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="商品名称" width="240px">
                <template v-slot="scope">
                  <a href="#" @click="navTo('/front/detail?id=' + scope.row.goodsId)">{{ scope.row.goodsName }}</a>
                </template>
              </el-table-column>
              <el-table-column prop="businessName" label="店铺">
                <template v-slot="scope">
                  <a href="#" @click="navTo('/front/business?id=' + scope.row.businessId)">{{
                      scope.row.businessName
                    }}</a>
                </template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="商品价格">
                <template v-slot="scope">
                  <span>￥{{ scope.row.goodsPrice }} / {{ scope.row.goodsUnit }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="num" label="选择数量">
                <template v-slot="scope">
                  <el-input-number v-model="scope.row.num" style="width: 100px" @change="handleChange(scope.row)"
                                   :min="1"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template v-slot="scope">
                  <el-button size="mini" type="danger" plain @click="del(scope.row.id)">移除商品</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination" style="margin-top: 20px;">
              <el-pagination
                  background
                  @current-change="handleCurrentChange"
                  :current-page="pageNum"
                  :page-sizes="[5, 10, 20]"
                  :page-size="pageSize"
                  layout="total, prev, pager, next"
                  :total="total">
              </el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      goodsData: [],
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      addressData: [],
      addressId: null,
      totalPrice: 0,
      selectedData: [],
    }
  },
  mounted() {
    this.loadGoods(1)
    this.loadAddress()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    navTo(url) {
      location.href = url
    },
    loadGoods(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/cart/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        if (res.code === '200') {
          this.goodsData = res.data?.list
          this.total = res.data?.total
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/cart/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.loadGoods(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    loadAddress() {
      this.$request.get('/address/selectAll').then(res => {
        if (res.code === '200') {
          this.addressData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleCurrentChange(pageNum) {
      this.loadGoods(pageNum)
    },
    handleChange(row) {
      this.totalPrice = 0
      this.selectedData.forEach(item => {
        this.totalPrice += item.goodsPrice * item.num
      })
    },
    handleSelectionChange(rows) {
      this.totalPrice = 0
      this.selectedData = rows
      // 计算总价
      this.selectedData.forEach(item => {
        this.totalPrice += item.goodsPrice * item.num
      })
    },
    pay() {
      if (!this.addressId) {
        this.$message.warning('请先选择收货地址')
        return;
      }
      if (!this.selectedData || this.selectedData.length === 0) {
        this.$message.warning('请先选择商品')
        return;
      }
      let data = {
        userId: this.user.id,
        addressId: this.addressId,
        status: '待发货',
        cartData: this.selectedData
      }
      this.$request.post('/orders/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('下单成功')
          this.loadGoods(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>