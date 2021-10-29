import Vue from 'vue'
import Router from 'vue-router'

import Layout from '@/components/Layout/Layout'

// Pages
import Dashboard from '@/pages/Dashboard/Dashboard'

// UI
import Cards from "@/pages/UI/Cards/Cards"
import ProcessDesigner from "@/pages/UI/ProcessDesigner"
import WorkItemHandler from "@/components/workspace/WorkItemHandler"

// Charts
import ChartsOverview from "@/pages/Charts/Overview/ChartsOverview"

import Login from "@/pages/Login/Login"

// E-Commerce
import ProductManagement from "@/pages/E-commerce/ProductManagement/ProductManagement"

//User
import WorkList from "@/pages/User/List/WorkList"
import Profile from "@/pages/User/Profile/Profile"

// Documentation
import DocLayout from "@/pages/Documentation/components/Layout/DocLayout"
// import {isAuthenticated} from "./mixins/auth";

// bpmn-portal
import UserPicker from '@/components/bpm-portal/UserPicker'
import UserAutocomplete from '@/components/bpm-portal/UserAutocomplete'


Vue.component('process-designer', ProcessDesigner);

Vue.component('user-picker', UserPicker);
Vue.component('user-autocomplete', UserAutocomplete);


Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },

    {
      name: 'Documentation',
      path: '/documentation',
      redirect: '/documentation/overview',
      component: DocLayout,
    },

    {
    path: '/',
    redirect: '/dashboard',
    name: 'Layout',
    component: Layout,
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
      },

        // Tables
      {
        path: '/participating/workLists/mine',
        name: 'workLists',
        component: ProductManagement
      },
      {
        path: '/participating/workLists/started',
        name: 'workLists',
        component: ProductManagement
      },
      {
        path: '/participating/workLists/us',
        name: 'workLists',
        component: ProductManagement
      },
      {
        path: '/participating/workLists/favorite',
        name: 'workLists',
        component: ProductManagement
      },
        // UI Elements
      {
        path: '/ui/cards',
        name: 'Cards',
        component: Cards
      },
      {
        path: '/ui/process-definition',
        name: 'processdefinition',
        component: ProcessDesigner,
      },
      {
        path: 'instance/:rootId/:id',
        name: 'instanceMonitor',
        component: ProcessDesigner,
        // beforeEnter: RouterGuard.requireUser,
        props: function (route) {
          return {
            // backend: backend,
            instanceId: route.params.id,
            rootInstanceId: route.params.rootId,
            monitor: true,
          }
        }
      },
      {
        path: '/workspace/worklist/:id*',
        name: 'WorkItemHandler',
        component: WorkItemHandler,
        props: function (route) {
          return {
            id: route.params.id
          }
        }
      },
       
        // Charts
      {
        path: '/charts/overview/basic',
        name: 'ChartsOverview',
        component: ChartsOverview
      },
      {
        path: '/charts/overview/multiDimensional',
        name: 'ChartsOverview',
        component: ChartsOverview
      },
      {
        path: '/charts/overview/EIP',
        name: 'ChartsOverview',
        component: ChartsOverview
      },

      // E-Commerce
      {
        path: '/work/worklist',
        name: 'WorkList',
        component: WorkList
      },
      
      {
        path: '/user/profile',
        name: 'UserProfile',
        component: Profile
      },
    ],
  },
  ],
});
