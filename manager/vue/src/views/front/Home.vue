<template>
  <div class="main-content">
    <div style="height: 60px; background-image: url('@/assets/imgs/1.png')"></div>
    <div style="display: flex">
      <div class="left"></div>
      <div style="width: 66%; margin-bottom: 50px; background-color: rgb(230,255,255)">
        <div style="color: rgb(254,1,55); margin: 15px 0 15px 15px; font-weight: bold; font-size: 16px">主题市场</div>
        <div style="display: flex; margin: 0 25px; height: 550px">
          <div style="flex: 2">
            <div style="display: flex; color: #666666FF; margin: 14px 0" v-for="item in typeData">
              <img :src="item.icon" style="height: 20px; width: 20px" alt="">
              <div style="margin-left: 10px; font-size: 14px"><a href="#" @click="navTo('/front/type?id=' + item.id)">{{
                  item.name
                }}</a></div>
            </div>
          </div>
          <div style="flex: 5; margin-top: 15px;">
            <div>
              <el-carousel height="300px" style="border-radius: 10px">
                <el-carousel-item v-for="item in carousel_top">
                  <img :src="item" alt=""
                       style="width: 100%; height: 300px; border-radius: 10px">
                </el-carousel-item>
              </el-carousel>
            </div>
            <div style="margin-top: 30px; display: flex">
              <div style="flex: 1">
                <el-carousel height="300px" style="border-radius: 10px">
                  <el-carousel-item v-for="item in carousel_left">
                    <img :src="item" alt=""
                         style="width: 100%; height: 200px; border-radius: 10px">
                  </el-carousel-item>
                </el-carousel>
              </div>
              <div style="flex: 1; margin-left: 5px">
                <el-carousel height="300px" style="border-radius: 10px">
                  <el-carousel-item v-for="item in carousel_right">
                    <img :src="item" alt=""
                         style="width: 100%; height: 200px; border-radius: 10px">
                  </el-carousel-item>
                </el-carousel>
              </div>
            </div>
          </div>
          <div
              style="flex: 3; margin-top: 15px; margin-left: 10px; background-color: rgb(243,243,243); border-radius: 10px">
            <div style="text-align: center; margin-top: 30px;">
              <img @click="navTo('/front/person')" :src="user.avatar" alt=""
                   style="width: 80px; height: 80px; border-radius: 50%">
              <div>欢迎登录, {{ user.name }}</div>
            </div>
            <div style="margin-top: 20px; padding: 0 15px; text-align: center">
              <img src="@/assets/imgs/right.png" alt="" style="height: 150px; width: 100%; border-radius: 20px">
            </div>
            <div
                style="margin: 20px 0 10px 10px; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
              <i class="el-icon-bell"></i>
              <span style="font-weight: bold">公告</span>
              <span style="color: #666666;">：{{ top }}</span>
            </div>
            <div style="display: flex; margin-top: 50px;">
              <div style="flex: 1; text-align: center">
                <a href="#" @click="navTo('/front/collect')"><img src="@/assets/imgs/收藏.png" alt=""
                                                                  style="height: 25px; width: 25px;">
                  <div>我的收藏</div>
                </a>
              </div>
              <div style="flex: 1; text-align: center">
                <div>
                  <a href="#" @click="navTo('/front/address')">
                    <img src="@/assets/imgs/店铺.png" alt="" style="height: 25px; width: 25px;">
                    <div>我的地址</div>
                  </a>
                </div>
              </div>
              <div style="flex: 1; text-align: center">
                <div>
                  <img src="@/assets/imgs/购物车.png" alt="" style="height: 25px; width: 25px;">
                  <div>我的购物车</div>
                </div>
              </div>
              <div style="flex: 1; text-align: center">
                <div>
                  <img src="@/assets/imgs/订单.png" alt="" style="height: 25px; width: 25px;">
                  <div>我的订单</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
            style="margin: 40px 0 0 15px; height: 40px; width: 130px; background-color: #e56c3c; font-size: 20px; color: white; font-weight: bold; line-height: 40px; text-align: center; border-radius: 20px; ">
          热卖商品
        </div>
        <div style="margin: 10px 5px 0 5px">
          <el-row>
            <el-col :span="5" v-for="item in goodsData">
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
      <div class="right"></div>
    </div>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      typeData: [],
      top: null,
      notice: [],
      goodsData: [],
      carousel_top: [
        require('@/assets/imgs/carousel-1.png'),
        require('@/assets/imgs/carousel-2.png'),
        require('@/assets/imgs/carousel-3.png'),
      ],
      carousel_left: [
        require('@/assets/imgs/carousel-4.png'),
        require('@/assets/imgs/carousel-5.png'),
        require('@/assets/imgs/carousel-6.png'),
      ],
      carousel_right: [
        require('@/assets/imgs/carousel-7.png'),
        require('@/assets/imgs/carousel-8.png'),
        require('@/assets/imgs/carousel-9.png'),
      ],
    }
  },
  mounted() {
    this.loadType()
    this.loadNotice()
    this.loadGoods()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        if (res.code === '200') {
          this.typeData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notice = res.data
        let i = 0
        if (this.notice && this.notice.length > 0) {
          this.top = this.notice[0].content
          setInterval(() => {
            this.top = this.notice[i].content
            i++
            if (i === this.notice.length) {
              i = 0
            }
          }, 2500) // 2.5秒切换一次
        }
      })
    },
    loadGoods() {
      this.$request.get('/goods/selectTop15').then(res => {
        if (res.code === '200') {
          this.goodsData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    navTo(path) {
      location.href = path
    }
  }
}
</script>

<style scoped>
.main-content {
  min-height: 100vh;
  /*overflow: hidden;*/
  background-size: 100%;
  background-image: url('@/assets/imgs/1.png');
}

.left {
  width: 17%;
  background-repeat: no-repeat;
  //background-image: url('@/assets/imgs/1.png');
}

.right {
  width: 17%;
  background-repeat: no-repeat;
  //background-image: url('@/assets/imgs/1.png')
}

.el-col-5 {
  width: 20%;
  max-width: 20%;
  padding: 10px 10px;
}
</style>
