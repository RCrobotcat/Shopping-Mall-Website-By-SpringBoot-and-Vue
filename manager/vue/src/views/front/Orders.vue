<template>
  <div class="main-content">
    <div
        style="width: 70%; background-color: white; margin: 30px auto; border-radius: 20px;">
      <div style="padding-bottom: 10px;">
        <div style="font-size: 18px; color: black; line-height: 80px; border-bottom: #cccccc 1px solid;">
          <div style="margin-left: 20px;">我的订单（共 {{ ordersData.length }} 个）</div>
        </div>
        <div style="margin: 20px 0; padding: 0 50px;">
          <div class="table">
            <el-table :data="ordersData" strip>
              <el-table-column label="商品图片" width="120px">
                <template v-slot="scope">
                  <div style="display: flex; align-items: center">
                    <el-image style="width: 80px; height: 60px; border-radius: 3px" v-if="scope.row.goodsImg"
                              :src="scope.row.goodsImg" :preview-src-list="[scope.row.goodsImg]"></el-image>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ordersId" label="订单编号"></el-table-column>
              <el-table-column prop="goodsName" label="商品名称" :show-overflow-tooltip="true">
                <template v-slot="scope">
                  <a href="#" @click="navTo('/front/detail?id=' + scope.row.goodsId)">
                    {{ scope.row.goodsName }}
                  </a>
                </template>
              </el-table-column>
              <el-table-column prop="businessName" label="店铺名称" :show-overflow-tooltip="true">
                <template v-slot="scope">
                  <a href="#" @click="navTo('/front/business?id=' + scope.row.businessId)">{{ scope.row.businessName }}</a>
                </template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="商品单价">
                <template v-slot="scope">
                  <span>￥{{ scope.row.goodsPrice }} / {{ scope.row.goodsUnit }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="下单数量">
                <template v-slot="scope">
                  <span>{{ scope.row.num }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="goodsPrice" label="订单总价">
                <template v-slot="scope">
                  <span>￥{{ scope.row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="收货人" :show-overflow-tooltip="true"></el-table-column>
              <el-table-column prop="useraddress" label="收货地址" :show-overflow-tooltip="true"></el-table-column>
              <el-table-column prop="phone" label="联系电话"></el-table-column>
              <el-table-column prop="status" label="订单状态"></el-table-column>
              <el-table-column label="操作" align="center" width="200">
                <template v-slot="scope">
                  <el-button size="mini" type="success" plain v-if="scope.row.status === '商家已发货, 待收货'" @click="updateStatus(scope.row, '订单已完成')">确认收货</el-button>
                  <el-button size="mini" type="warning" plain v-if="scope.row.status === '订单已完成'" @click="addComment(scope.row)">评价商品</el-button>
                  <el-button size="mini" type="danger" plain @click="del(scope.row.id)">取消订单</el-button>
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

    <el-dialog title="请输入评价内容" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="评价内容" prop="content">
          <el-input type="textarea" v-model="form.content" placeholder="评价"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      ordersData: [],
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      form: {},
      fromVisible: false,
    }
  },
  mounted() {
    this.loadOrders(1)
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    navTo(url) {
      location.href = url
    },
    loadOrders(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        if (res.code === '200') {
          this.ordersData = res.data?.list
          this.total = res.data?.total
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/orders/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.loadOrders(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleCurrentChange(pageNum) {
      this.loadOrders(pageNum)
    },
    save() {   // 保存触发的逻辑  它会触发新增或者更新
      let data = {
        userId: this.user.id,
        businessId: this.form.businessId,
        goodsId: this.form.goodsId,
        content: this.form.content,
      }
      this.$request.post('/comment/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('评价成功')
          this.fromVisible = false;
          this.updateStatus(this.form, '已评价')
          this.form = {}
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    updateStatus(row, status){
      this.form = row;
      this.form.status = status;
      this.$request.put('/orders/update', this.form).then(res=>{
        if (res.code === '200') {
          this.$message.success('操作成功')
          this.loadOrders(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    addComment(row){
      this.fromVisible = true;
      this.form = row;
    }
  }
}
</script>