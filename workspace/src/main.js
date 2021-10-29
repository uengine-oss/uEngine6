/*eslint-disable*/
import Vue from 'vue'
import App from './App.vue'
import router from './Routes'
import store from './store/index'
import vuetify from './plugins/vuetify'
import axios from 'axios'
import config from "./config";
import * as VueGoogleMaps from 'vue2-google-maps'
import {AuthMixin} from "./mixins/auth"
import Opengraph from './components/opengraph'
import BpmnVue from './components/bpmn'

import ObjectGrid from './components/ObjectGrid'
import ObjectForm from './components/ObjectForm'
import ObjectFormBoolean from './components/ObjectFormBoolean'
import ObjectFormSelect from './components/ObjectFormSelect'

import ServiceLocator from './components/ServiceLocator'

// import bmpnVue from './components/bpmn/BpmnVue'
// import Opengraph from './components/opengraph'
Vue.use(Opengraph);
Vue.use(BpmnVue);

Vue.prototype.$http = axios

axios.defaults.baseURL = config.baseURLApi;
axios.defaults.headers.common['Content-Type'] = "application/json";
const token = localStorage.getItem('accessToken');
if (token) {
  axios.defaults.headers.common['Authorization'] = "Bearer " + token;
}


// Vue.component('bmpnVue', bmpnVue)
// Vue.use(Opengraph)
Vue.component('object-grid', ObjectGrid);
Vue.component('object-form', ObjectForm);
Vue.component('object-form-select', ObjectFormSelect);
Vue.component('object-form-boolean', ObjectFormBoolean);
Vue.component('service-locator', ServiceLocator);

Vue.use(VueGoogleMaps, {
  load: {
    key: 'AIzaSyB7OXmzfQYua_1LEhRdqsoYzyJOPh9hGLg',
  },
});
Vue.mixin(AuthMixin);

Vue.config.productionTip = false;
Vue.prototype.$EventBus = new Vue()
new Vue({
  vuetify,
  router,
  render: h => h(App), store
}).$mount('#app');

