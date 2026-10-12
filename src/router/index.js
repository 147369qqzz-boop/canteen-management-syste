import {createRouter, createWebHistory} from 'vue-router'

import { getLoginUser } from '@/utils/auth'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'carousel', component: () => import('@/views/manager/Carousel.vue')},
        { path: 'collect', component: () => import('@/views/manager/Collect.vue')},
        { path: 'recharge', component: () => import('@/views/manager/Recharge.vue')},
        { path: 'orders', component: () => import('@/views/manager/Orders.vue')},
        { path: 'comment', component: () => import('@/views/manager/Comment.vue')},
        { path: 'dataManager', component: () => import('@/views/manager/DataManager.vue')},
        { path: 'notice', component: () => import('@/views/manager/Notice.vue')},
        { path: 'feedback', component: () => import('@/views/manager/Feedback.vue')},
        { path: 'canteen', component: () => import('@/views/manager/Canteen.vue')},
        { path: 'stallManage', component: () => import('@/views/manager/StallManage.vue')},
      ]
    },

      {
          path: '/front',
          component: () => import('@/views/Front.vue'),
          redirect: '/front/home',
          children: [
              { path: 'home', component: () => import('@/views/front/Home.vue')},
              { path: 'person', component: () => import('@/views/front/Person.vue')},
              { path: 'password', component: () => import('@/views/front/Password.vue')},
              { path: 'goods', component: () => import('@/views/front/Goods.vue')},
              { path: 'goodsDetail', component: () => import('@/views/front/GoodsDetail.vue')},
              { path: 'stalls', component: () => import('@/views/front/Stalls.vue')},
              { path: 'stallGoods', component: () => import('@/views/front/StallGoods.vue')},
              { path: 'diet', component: () => import('@/views/front/Diet.vue')},
              { path: 'queue', component: () => import('@/views/front/Queue.vue')},
              { path: 'userCollect', component: () => import('@/views/front/UserCollect.vue')},
              { path: 'userRecharge', component: () => import('@/views/front/UserRecharge.vue')},
              { path: 'cart', component: () => import('@/views/front/Cart.vue')},
              { path: 'userOrders', component: () => import('@/views/front/UserOrders.vue')},
              { path: 'userComment', component: () => import('@/views/front/UserComment.vue')},
              { path: 'userFeedback', component: () => import('@/views/front/UserFeedback.vue')},
          ]
      },

      // 商家端（6大功能模块）
      {
          path: '/business',
          component: () => import('@/views/BusinessLayout.vue'),
          redirect: '/business/dashboard',
          children: [
              { path: 'dashboard', component: () => import('@/views/business/Home.vue') },
              { path: 'dishes', component: () => import('@/views/business/Goods.vue') },
              { path: 'orders', component: () => import('@/views/business/Orders.vue') },
              { path: 'revenue', component: () => import('@/views/business/Revenue.vue') },
              { path: 'reviews', component: () => import('@/views/business/Comments.vue') },
              { path: 'settings', component: () => import('@/views/business/Person.vue') },
              { path: 'status', component: () => import('@/views/business/Status.vue') },
          ]
      },
    { path: '/login', component: () => import('@/views/Login.vue') },
      { path: '/register', component: () => import('@/views/Register.vue') }
  ]
})

router.afterEach(() => {
    window.scroll({top:0, behavior:'smooth'})
})


export default router
