<template>
  <v-container fluid class="user-list mt-3">
    <v-row class="ma-0">
      <v-col cols="12">
        <v-card class="tasks-card ma-1" v-if="!selectWorkItem">
          <v-card-title class="pa-5 pb-3">
            <p>Inbox</p>
            <v-spacer></v-spacer>
            <v-menu>
              <template v-slot:activator="{ on, attrs }">
                <v-select
                  class="main-chart-select font-weight-regular greyMedium--text"
                  v-model="sel2"
                  :value="taskSelect[0]"
                  v-bind="attrs"
                  v-on="on"
                  dense
                  flat
                  single-line
                  hide-details
                  :items="taskSelect"
                ></v-select>
              </template>
            </v-menu>
          </v-card-title>
          <v-card-text class="pa-5 pt-0 pb-2">
            <v-row no-gutters>
              <v-tabs color="secondary">
                <v-tabs-slider></v-tabs-slider>
                <v-tab
                  v-for="(tab, i) in taskTabs"
                  :key="i"
                  :href="'#tab-' + tab.tabLink"
                  class="text-capitalize font-weight-regular">
                  {{ tab.tabName }}
                </v-tab>
                <v-tab-item
                  value="tab-today"
                  class="pt-0">
                  <v-row no-gutters class="flex-column flex-nowrap overflow-hidden pr-0"
                    v-for="(work, i) in worklist"
                    :key="i"
                    style="width: 100%; cursor:pointer;"
                    :class="{ done:false }"
                    @click="selectedWorkItem(work)"
                  >
                    <v-divider></v-divider>
                    <v-col
                      cols="12"
                      class="d-flex justify-space-between align-center py-3">
                      <div class="d-flex align-center">
                        <p
                          class="task-time mb-0 "
                          style="font-size: 10px">{{ work.startDate }}</p>
                        <v-icon class="task-circle ml-1" color="green">mdi-circle-medium</v-icon>
                          <p class="task-text greyBold--text mb-0 font-weight-medium" style="line-height: 25px;">{{ work.title }}</p>
                      </div>
                      <v-menu>
                        <template v-slot:activator="{ on, attrs }">
                          <v-btn
                            icon
                            v-bind="attrs"
                            v-on="on">
                            <v-icon color="greyTint">mdi-dots-vertical</v-icon>
                          </v-btn>
                        </template>
                        <v-list>
                          <v-list-item
                            v-for="(item, i) in taskMenu"
                            :key="i"
                            @click="() => {}">
                            <v-list-item-title>{{ item }}</v-list-item-title>
                          </v-list-item>
                        </v-list>
                      </v-menu>
                    </v-col>
                  </v-row>
                </v-tab-item>
              </v-tabs>
            </v-row>
          </v-card-text>
        </v-card>
        <!-- <v-card class="ma-1"> -->
          <!-- <v-card-text> -->
              <!-- <v-btn @click="selectWorkItem = false">test</v-btn> -->
            <work-item-handler :selectWorkItem="selectWorkItem" />
          <!-- </v-card-text> -->
        <!-- </v-card> -->
      </v-col>

      <!-- <v-col cols="8"> -->
        <!-- <v-card class="ma-1" v-if="selectWorkItem">
          <v-card-text>
              <v-btn @click="selectWorkItem = false">test</v-btn>
            <work-item-handler />
          </v-card-text>
        </v-card> -->
      <!-- </v-col> -->
    </v-row>
  </v-container>
</template>

<script>
  import config from "@/config";
  import WorkItemHandler from '../../../components/workspace/WorkItemHandler.vue';
  //import { mapActions, mapState } from 'vuex';

  export default {
  components: { WorkItemHandler },
  name: 'WorkList',
  props: {
    path: String
  },
  data() {
    return {
      config,
      snackbar: null,
      dialog: false,
      headers: [
        { text: 'Image', value: 'image', sortable: false },
        { text: 'First Name', value: 'firstName' },
        { text: 'Last Name', value: 'lastName' },
        { text: 'Role', value: 'role' },
        { text: 'Company', value: 'company' },
        { text: 'Email', value: 'email' },
        { text: 'Status', value: 'disabled' },
        { text: 'Create', value: 'createdAt' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      selected: [],
      switch1: true,
      role: ['admin', 'user'],
      editedIndex: -1,
      editedItem: {
        firstName: '',
        image: '',
        role: '',
        company: '',
        email: '',
        disabled: '',
        create: '',
      },
      defaultItem: {
        firstName: '',
        image: '',
        role: '',
        company: '',
        email: '',
        disabled: '',
        create: '',
      },
      images: [
        require('@/assets/img/user/avatars/1.png'),
        require('@/assets/img/user/avatars/2.png'),
        require('@/assets/img/user/avatars/3.png'),
        require('@/assets/img/user/avatars/4.png'),
        require('@/assets/img/user/avatars/5.png'),
      ],
      itemPerPage: 5,
      text: '',
      notification: 'This page is only available in Vue Material Admin Full with Node.js integration!',
      worklist: null,
      taskSelect: [
        'All tasks',
        'Done',
        'In progress'
      ],
      taskMenu: [
        'Edit',
        'Copy',
        'Delete',
        'Share',
      ],
      taskTabs: [
        {
          tabLink: 'today',
          tabName: 'Today',
        },
      ],
      sel2: 'All tasks',
      workItemPath: null,
      selectWorkItem: false,
    }
  },
  mounted(){
    var me = this
    this.$http.get("http://bpm.uengine.io:9090/worklist/search/findToDo").then((result)=>{
      me.worklist = result.data._embedded.worklist
    })
    me.$EventBus.$on("unselectWorkItem", function(data){
        if(data){
            me.selectWorkItem = false
        }
    })
  },
  computed: {},

  watch: {},
  created () {},

  methods: {
    async selectedWorkItem(work){
      var me = this
      me.selectWorkItem = true
      // var axios = require("axios");
      // var test = await axios.get(work._links.workItem.href)
    //   console.log(work)
      // this.$router.push({
      //     path: '/workspace/worklist/' + work.rootInstId
      // })
      me.workItemPath = work._links.workItem.href
      me.$EventBus.$emit("setPath", me.workItemPath)
    },
  }
}
</script>


