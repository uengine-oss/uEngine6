<template>
    <v-container fluid class="product-management mt-4 px-1">
        <v-row>
            <v-col cols="12">
                <v-card class="mb-1">
                    <v-card-title class="pa-5 pb-3">
                        <p>참여중인 프로세스</p>
                        <!-- <span class="grey--text font-weight-regular subtitle-2 pt-1">{{'\xa0 (' +  products.length }} total) </span> -->
                        <v-spacer></v-spacer>
                        <div>
                            <v-text-field
                                    v-model="search"
                                    append-icon="mdi-magnify"
                                    label="Search"
                                    single-line
                                    hide-details
                                    class="search"
                            ></v-text-field>
                        </div>
                    </v-card-title>
                    <v-card-text class="px-5">
                        <!-- :loading="isReceiving || isDeleting" -->
                        <v-data-table
                                v-if="workList"
                                loading-text="Loading... Please wait"
                                class="product-table"
                                show-select
                                show-expand
                                single-expand
                                :expanded.sync="expanded"
                                :headers="headers"
                                :items="workList"
                                :search="search"
                                item-key="rootInstId"
                                sort-by="id">
                            <template v-slot:top>
                                <v-dialog v-model="dialog" max-width="500px">
                                    <template v-slot:activator="{ on, attrs }">
                                        <v-btn
                                                color="success"
                                                dark
                                                class="my-4 button-shadow"
                                                v-bind="attrs"
                                                v-on="on"
                                        >Start Process
                                        </v-btn>
                                    </template>
                                    <v-card class="edit-dialog">
                                        <v-card-title>
                                            <!-- <span class="headline">{{ formTitle }}</span> -->
                                        </v-card-title>
                                        <v-card-text>
                                            <v-container>
                                                <v-row>
                                                    <v-col cols="12" sm="6">
                                                        <v-text-field outlined v-model="editedItem.title"
                                                                      label="Product name"></v-text-field>
                                                    </v-col>
                                                    <v-col cols="12" sm="6">
                                                        <v-text-field outlined v-model="editedItem.subtitle"
                                                                      label="Subtitle"></v-text-field>
                                                    </v-col>
                                                    <v-col cols="12" sm="6" md="4">
                                                        <v-text-field outlined v-model="editedItem.price"
                                                                      label="Price"></v-text-field>
                                                    </v-col>
                                                    <v-col cols="12" sm="6" md="4">
                                                        <v-text-field :rules="ratingRules" outlined
                                                                      v-model="editedItem.rating" type="number"
                                                                      label="Rating"></v-text-field>
                                                    </v-col>
                                                    <v-col cols="12" sm="6" md="4">
                                                        <v-select outlined :items="images" v-model="editedItem.img"
                                                                  label="Image">
                                                            <template v-slot:item="{ item }">
                                                                <v-img :src="item" width="50"
                                                                       style="margin: 2px"></v-img>
                                                            </template>
                                                        </v-select>
                                                    </v-col>
                                                </v-row>
                                            </v-container>
                                        </v-card-text>
                                        <v-card-actions>
                                            <v-spacer></v-spacer>
                                            <v-btn color="primary" outlined @click="close">Cancel</v-btn>
                                            <!-- <v-btn color="success" class="button-shadow" @click="save">Save</v-btn> -->
                                        </v-card-actions>
                                    </v-card>
                                </v-dialog>
                            </template>
                            <template v-slot:item.defId="{ item }">
                                {{getProcessNameBydefId(item) }}
                            </template>
                            <template v-slot:item.title="{ item }">
                                <a class="primaryConst--text">{{ item.title }}</a>
                            </template>
                            <template v-slot:item.rating="{ item }">
                                <div class="d-flex align-center">
                                    <span class="warning--text" style="font-size: 1rem">{{ item.rating }} </span>
                                    <v-icon size="20" color="warning">mdi-star</v-icon>
                                </div>
                            </template>
                            <template v-slot:expanded-item="{ headers, item }">
                                <td :colspan="headers.length" >
                                    <instance-handler :id="item.rootInstId"></instance-handler>
                                </td>
                            </template>
                            <!-- <template v-slot:item.api="{ item }"> -->
                            <!-- <v-btn
                              class="mr-3 button-shadow"
                              @click="editItem(item)"
                              small
                              color="success"
                            >위임
                            </v-btn> -->
                            <!-- <v-btn
                              @click="deleteItem(item)"
                              small
                              class="button-shadow"
                              color="secondary"
                            >Delete
                            </v-btn> -->
                            <!-- </template> -->
                        </v-data-table>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
        <v-snackbar
                v-model="snackbar"
                color="success"
                right
                top
                style="top: 50px">
            {{ text }}
            <template v-slot:action="{ attrs }">
                <v-btn
                        dark
                        text
                        v-bind="attrs"
                        @click="snackbar = false">
                    Close
                </v-btn>
            </template>
        </v-snackbar>
    </v-container>
</template>

<script>
    import config from "@/config";
    import InstanceHandler from "../../../components/workspace/InstanceHandler";
    //import { mapActions, mapState } from 'vuex';

    export default {
        name: 'ProductManagement',
        components: {InstanceHandler},
        data() {
            return {
                snackbar: null,
                search: '',
                dialog: false,
                instanceList: null,
                workList: null,
                expanded: [],
                headers: [
                    {//defName, status, rootInstId, startedDate
                        text: 'ID',
                        align: 'start',
                        value: 'rootInstId',
                    },
                    {text: '프로세스', value: 'defId', sortable: false},
                    {text: '태스크명', value: 'title'},
                    {text: '인스턴스명', value: 'defName'},
                    {text: '진행일수', value: 'startDate'},
                    {text: '중요도', value: 'rating'},
                    {text: '상태', value: 'status'},
                    {text: 'Actions', value: 'api', sortable: false},
                ],
                ratingRules: [
                    v => !!v || "This field is required",
                    v => (v && v >= 0) || "Rating should be above 0",
                    v => (v && v <= 5) || "Max Rating not be above 5",
                ],
                selected: [],
                editedIndex: -1,
                editedItem: {
                    title: '',
                    img: '',
                    subtitle: '',
                    price: '',
                    rating: '',
                },
                defaultItem: {
                    title: '',
                    img: '',
                    subtitle: '',
                    price: '',
                    rating: '',
                },
                images: [
                    require('@/assets/img/e-commerce/low/1.png'),
                    require('@/assets/img/e-commerce/low/2.png'),
                    require('@/assets/img/e-commerce/low/3.png'),
                    require('@/assets/img/e-commerce/low/4.png'),
                    require('@/assets/img/e-commerce/low/5.png'),
                    require('@/assets/img/e-commerce/low/6.png')
                ],
                notification: 'This page is only available in Vue Material Admin Full with Node.js integration!'
            }
        },
        computed: {
            // ...mapState('products', ['products', "isReceiving", "isDeleting", "idToDelete", "productMessage", "isUpdating"]),
            // formTitle () {
            //   return this.editedIndex === -1 ? 'New Item' : 'Edit Item'
            // },
        },
        watch: {
            dialog(val) {
                val || this.close()
            },
            // productMessage() {
            //   if (this.productMessage !== '') {
            //     this.notification = this.productMessage
            //     this.addSuccessNotification()
            //   }
            // }
        },
        created() {
            this.addSuccessNotification()
        },
        mounted() {
            // this.getProductsRequest()
            var me = this
            try {
                me.$http.get("http://bpm.uengine.io/worklist/search/findToDo")
                    .then((result) => {
                        me.workList = result.data._embedded.worklist

                        // absTrcTag: null
                        // actType: null
                        // defId: "trouble ticket.xml"
                        // defName: "purchase order"
                        // delegated: null
                        // description: null
                        // dispatchOption: 0
                        // dispatchParam1: null
                        // dueDate: "2021-10-11T07:34:16.996+0000"
                        // endDate: null
                        // endpoint: "user01@gmail.com"
                        // execScope: null
                        // ext1: null
                        // ext2: null
                        // ext3: null
                        // ext4: null
                        // ext5: null
                        // instId: 1
                        // parameter: null
                        // prevUserName: null
                        // priority: 1
                        // readDate: null
                        // refRoleName: "null"
                        // resName: "user01@gmail.com"
                        // roleName: "initiator"
                        // rootInstId: 1
                        // saveDate: null
                        // startDate: "2021-10-06T07:34:16.995+0000"
                        // status: "NEW"
                        // title: "구매요청"
                        // tool: "defaultHandler"
                        // trcTag: "2"
                        // urget: null
                    })
                // me.$http.get("http://bpm.uengine.io:9090/instances/search/findFilterICanSee")
                //         .then((result) => {
                //           me.instanceList = result.data._embedded.instances
                //           me.$http.get("http://bpm.uengine.io:9090/worklist/search/findToDo")
                //                   .then((result) => {
                //                             me.workList = result.data._embedded.worklist
                //                             if (me.instanceList) {
                //                               me.instanceList.forEach(function (instance, index) {
                //                                 me.workList.forEach(function (work) {
                //                                   if (instance.rootInstId == work.rootInstId) {
                //                                     me.instanceList[index].title = work.title
                //                                     me.instanceList.__ob__.dep.notify();
                //                                   }
                //                                 })
                //                               })
                //                             }
                //                           }
                //                   )
                //         })
            } catch (e) {
                alert(e)
            }


        },
        methods: {
            selectInstance: function (item) {
                var uri = item._links.self.href
                this.$router.push({
                    path: '/workspace/instance/' + this.getIdFromUri(uri)
                })
            },
            getIdFromUri: function (uri) {
                var split = uri.split('/');
                return split[split.length - 1];
            },
            getProcessNameBydefId(item) {
                var name = item.defId
                name = name.split('.xml')[0]
                return name
            },
            // ...mapActions('products',
            //   [
            //     "getProductsRequest",
            //     "deleteProductRequest",
            //     "updateProductRequest",
            //     "createProductRequest"
            //   ]
            // ),
            // editItem (item) {
            //   this.editedIndex = this.products.indexOf(item)
            //   this.editedItem = Object.assign({}, item)
            //   this.dialog = true
            // },
            // deleteItem (item) {
            //   if (!config.isBackend ) {
            //     confirm('Are you sure you want to delete this item?') && this.products.splice(item.id, 1)
            //   } else {
            //     confirm('Are you sure you want to delete this item?') && this.deleteProductRequest({id: item.id})
            //   }
            // },
            close() {
                this.dialog = false
                this.$nextTick(() => {
                    this.editedItem = Object.assign({}, this.defaultItem)
                    this.editedIndex = -1
                })
            },
            // save () {
            //   if (this.editedIndex > -1) {
            //     !config.isBackend ?
            //     Object.assign(this.products[this.editedIndex], this.editedItem)
            //     :
            //     this.updateProductRequest(this.editedItem)
            //   } else {
            //     !config.isBackend ?
            //     this.products.push(this.editedItem)
            //     :
            //     this.createProductRequest(this.editedItem)
            //   }
            //   this.close()
            // },
            addSuccessNotification() {
                this.snackbar = true;
                this.color = config.light.success;
                this.text = this.notification;
            },
        },
    }
</script>


