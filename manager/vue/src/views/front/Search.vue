<template>
  <div class="main-content">
    <div
        style="display: flex; width: 70%; background-color: white; margin: 30px auto; border-radius: 20px;">
      <div style="flex: 1; padding: 0 20px;">
        <div style="font-size: 18px; color: black; line-height: 80px; border-bottom: #cccccc 1px solid;">
          搜索" {{ searchData }} "的结果
        </div>
        <div style="margin: 20px 0; ">
          <el-row :gutter="20">
            <el-col :span="6" v-for="item in goodsData">
              <img @click="navTo('/front/detail?id=' + item.id)" :src="item.img" alt=""
                   style="width: 100%; height: 175px; border-radius: 10px; border: #cccccc 1px solid;">
              <div
                  style="margin-top: 10px; width: 160px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; font-weight: 500; font-size: 16px; color: #000000ff">
                {{ item.name }}
              </div>
              <div style="margin-top: 5px; color: #FE7843; font-size: 20px;">¥ {{ item.price }} / {{ item.unit }}</div>
            </el-col>
          </el-row>
        </div>
      </div>
      <!--   以下部分要利用推荐算法   -->
      <div style="width: 250px; border-left: #cccccc 1px solid; padding: 0 20px;">
        <div style="font-size: 18px; color: black; line-height: 80px; border-bottom: #cccccc 1px solid;">猜你喜欢</div>
        <div v-for="item in recommendData" style="margin: 20px 0; padding: 0 10px;">
          <img @click="navTo('/front/detail?id=' + item.id)" :src="item.img" alt="" style="width: 100%; height: 180px; border-radius: 10px; border: #cccccc 1px solid;">
          <div style="margin-top: 10px; width: 160px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; font-weight: 500; font-size: 16px; color: #000000ff">
            {{ item.name }}
          </div>
          <div style="margin-top: 5px; color: #FE7843; font-size: 20px;">
            ¥ {{ item.price }} / {{ item.unit }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {

  data() {
    let searchData = this.$route.query.searchData
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      searchData: searchData,
      goodsData: [],
      recommendData: [],
    }
  },
  mounted() {
    this.loadGoods()
    this.loadRecommend()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadGoods() {
      this.$request.get('/goods/selectByName?name=' + this.searchData).then(res => {
        if (res.code === '200') {
          this.goodsData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadRecommend(){
      this.$request.get('/goods/selectRecommend').then(res => {
        if (res.code === '200') {
          this.recommendData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    navTo(url) {
      location.href = url
    }
  }
}
</script>