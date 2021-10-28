<template>
    <v-layout wrap>
        <v-navigation-drawer absolute right style="width:600px; height:100%; margin-top:-62px; max-height:600px; overflow:auto">

            <!--  상단 이미지 및 선택 타이틀 이름-->
            <v-list class="pa-1">
                <v-list-item>
                    <v-tabs v-if="item._instanceInfo">
                        <v-tab v-if="item._instanceInfo">
                            <v-list-item-title>Instance Info</v-list-item-title>
                        </v-tab>
                    </v-tabs>
                    <v-tabs v-model="tab">
                        <v-tab v-for="tab in tabs" :key="tab" :id="tab.toLowerCase() + _uid">
                            <v-list-item-title>{{ tab }}</v-list-item-title>
                        </v-tab>
                    </v-tabs>
                    
                    <v-btn icon @click.native="closePanel()">
                        <v-icon color="grey lighten-1">mdi-close</v-icon>
                    </v-btn>
                </v-list-item>
            </v-list>

            <v-simple-table  v-if="item._instanceInfo">
                <template v-slot:default>
                    <thead>
                        <tr>
                            <th class="text-left">Key</th>
                            <th class="text-left">Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(row, index) in item._instanceInfo" :key="index">
                            <td>{{ index }}</td>
                            <td>{{item._instanceInfo[index] }}</td>
                        </tr>
                    </tbody>
                </template>
            </v-simple-table>

            <v-list>
                <v-divider></v-divider>
                <div v-if="tab == 0">
                    <v-card flat>
                        <v-card-text>
                            <v-text-field
                                    v-if="tracingTag !== null"
                                    v-model="tracingTag"
                                    label="액티비티 ID*"
                                    counter="50"
                                    autofocus
                            ></v-text-field>
                            <v-text-field
                                    v-if="item._type == 'org.uengine.kernel.bpmn.EndEvent' || item._type == 'org.uengine.kernel.bpmn.TerminateEndEvent'"
                                    v-model="_item.name.text"
                                    label="End Status Name"
                            ></v-text-field>
                            <v-text-field
                                    v-else
                                    v-model="_item.name.text"
                                    label="액티비티 명"
                            ></v-text-field>
                            <v-switch
                                    v-if="item._type == 'org.uengine.kernel.bpmn.EndEvent'"
                                    v-model="_item.stopAllTokens"
                                    label="Stops all running tokens"
                            ></v-switch>
                            <v-text-field
                                    v-model="_item.retryDelay"
                                    label="retryDelay"
                                    type="number"
                            ></v-text-field>
                            <v-text-field
                                    v-if="item._type == 'org.uengine.kernel.bpmn.CatchingErrorEvent' || item._type == 'org.uengine.kernel.bpmn.ConditionalCatchEvent'
                                            || item._type == 'org.uengine.kernel.bpmn.EscalationIntermediateCatchEvent' || item._type == 'org.uengine.kernel.bpmn.SignalIntermediateCatchEvent'
                                            || item._type == 'org.uengine.kernel.view.DefaultActivityView'"
                                    v-model="_item.attachedToRef"
                                    label="Attach Activity ID"
                            ></v-text-field>
                            <v-text-field
                                    v-if="item._type == ''"
                                    v-model="_item['test']"
                                    label="Attach Activity ID"
                            ></v-text-field>
                            
                        </v-card-text>
                    </v-card>
                </div>
                <div v-if="tab == 1">
                    <v-card flat>
                        <v-card-text>
                            <v-row>
                                <v-col cols="6" class="py-0">
                                    <v-text-field
                                            label="x"
                                            v-model="x"
                                            type="number"
                                    ></v-text-field>
                                </v-col>
                                <v-col cols="6" class="py-0">
                                    <v-text-field
                                            label="y"
                                            v-model="y"
                                            type="number"
                                    ></v-text-field>
                                </v-col>
                            </v-row>
                            <v-row>
                                <v-col cols="6" class="py-0">
                                    <v-text-field
                                            label="width"
                                            v-model="width"
                                            type="number"
                                    ></v-text-field>
                                </v-col>
                                <v-col cols="6" class="py-0">
                                    <v-text-field
                                            label="height"
                                            v-model="height"
                                            type="number"
                                    ></v-text-field>
                                </v-col>
                            </v-row>
                            <v-text-field
                                    v-for="(item, idx) in style" :key="idx"
                                    v-model="style[idx].value"
                                    :label="item.key"
                            ></v-text-field>
                        </v-card-text>
                    </v-card>
                </div>
            </v-list>

        </v-navigation-drawer>
    </v-layout>
<!--                 ----------------------------------------------------------------->
<!--        <md-sidenav class="md-right" ref="rightSidenav" @open="open('Right')" @close="close('Right')" id="test">-->
<!--            <md-tabs v-if="navigationDrawer">-->

<!--                <md-tab md-label="Instance Info" v-if="item._instanceInfo">-->
<!--                    <md-table>-->
<!--                        <md-table-header>-->
<!--                            <md-table-row>-->
<!--                                <md-table-head>Key</md-table-head>-->
<!--                                <md-table-head>Value</md-table-head>-->
<!--                            </md-table-row>-->
<!--                        </md-table-header>-->

<!--                        <md-table-body>-->
<!--                            <md-table-row v-for="(row, index) in item._instanceInfo" :key="index">-->
<!--                                <md-table-cell>{{index}}</md-table-cell>-->
<!--                                <md-table-cell>{{item._instanceInfo[index]}}</md-table-cell>-->
<!--                            </md-table-row>-->
<!--                        </md-table-body>-->
<!--                    </md-table>-->
<!--                </md-tab>-->

<!--                <md-tab :id="'properties' + _uid" md-label="Properties">-->

<!--                    <md-input-container v-if="tracingTag !== null">-->
<!--                        <label>액티비티 ID</label>-->
<!--                        <md-input v-model="tracingTag"-->
<!--                                type="text"-->
<!--                                maxlength="50"-->
<!--                                required></md-input>-->
<!--                    </md-input-container>-->

<!--                    <slot name="properties-contents">-->
<!--                    </slot>-->
<!--                </md-tab>-->

<!--                <slot name="additional-tabs">-->
<!--                </slot>-->

<!--                <md-tab :id="'visual' + _uid" md-label="Visual">-->
<!--                    <div v-if="item.elementView">-->
<!--                        <md-layout>-->
<!--                            <md-layout>-->
<!--                                <md-input-container>-->
<!--                                    <label>x</label>-->
<!--                                    <md-input type="number"-->
<!--                                            v-model.number="x"></md-input>-->
<!--                                </md-input-container>-->
<!--                            </md-layout>-->
<!--                            <md-layout>-->
<!--                                <md-input-container>-->
<!--                                    <label>y</label>-->
<!--                                    <md-input type="number"-->
<!--                                            v-model.number="y"></md-input>-->
<!--                                </md-input-container>-->
<!--                            </md-layout>-->
<!--                        </md-layout>-->
<!--                        <md-layout>-->
<!--                            <md-layout>-->
<!--                                <md-input-container>-->
<!--                                    <label>width</label>-->
<!--                                    <md-input type="number"-->
<!--                                            v-model.number="width"></md-input>-->
<!--                                </md-input-container>-->
<!--                            </md-layout>-->
<!--                            <md-layout>-->
<!--                                <md-input-container>-->
<!--                                    <label>height</label>-->
<!--                                    <md-input type="number"-->
<!--                                            v-model.number="height"></md-input>-->
<!--                                </md-input-container>-->
<!--                            </md-layout>-->
<!--                        </md-layout>-->
<!--                    </div>-->

<!--                <md-input-container v-for="(item, index) in style"-->
<!--                            :item="item"-->
<!--                            :index="index">-->
<!--                    <label>{{item.key}}</label>-->
<!--                    <md-input type="text"-->
<!--                        v-model="style[index].value"></md-input>-->
<!--                </md-input-container>-->
<!--            </md-tab>-->

<!--        </md-tabs>-->
<!--    </md-sidenav>-->
</template>

<script>
    import BpmnVueFinder from './BpmnVueFinder'
    import BpmnComponentFinder from './BpmnComponentFinder'

    export default {
        mixins: [BpmnVueFinder, BpmnComponentFinder],
        name: 'bpmn-property-panel',
        props: {
            drawer: {
                default: function () {
                    return false;
                },
                type: Boolean
            },
            item: Object
        },
        computed: {},
        data() {
            return {
                navigationDrawer: this.drawer,
                _item: this.item,
                preventWatch: false,
                x: null,
                y: null,
                width: null,
                height: null,
                style: [],
                active: null,
                tracingTag: null,
                tabs: [ 'Properties', 'Visual' ],
                tab: null
            }
        },
        created: function () {
        },
        mounted: function () {
            console.log(this.item)

            this._item = this.item;

            if (this.item.elementView) {
                this.x = this.item.elementView.x;
                this.y = this.item.elementView.y;
                this.width = this.item.elementView.width;
                this.height = this.item.elementView.height;
            }

            //맵 형식의 스타일을 어레이타입으로 변형한다.
            var view = this.item.elementView || this.item.relationView;
            var style = [];
            if (view.style) {
                var itemStyle = JSON.parse(view.style);
                if (!$.isEmptyObject(itemStyle)) {
                    for (var key in itemStyle) {
                        style.push({
                            key: key,
                            value: itemStyle[key]
                        });
                    }
                }
                this.style = style;
            }

            if (this.item.tracingTag) {
                this.tracingTag = this.item.tracingTag;
            }

            //bpmnVue 에 프로퍼티 에디팅중임을 알린다.
            //프로퍼티 에디팅 중 데피니션 변화는 히스토리에 기록된다.
            this.bpmnVue.propertyEditing = true;
            this.$emit('update:drawer', true);
        },
        watch: {
            drawer: function (val) {
                console.log(val)
                this.navigationDrawer = val;
            },
            //프로퍼티 창이 오픈되었을 때 모델값을 새로 반영한다.
            navigationDrawer: {
                handler: function (val, oldval) {
                    console.log('val', val);
                    if (val == true) {
                        this._item = this.item;

                        if (this.item.elementView) {
                            this.x = this.item.elementView.x;
                            this.y = this.item.elementView.y;
                            this.width = this.item.elementView.width;
                            this.height = this.item.elementView.height;
                        }

                        //맵 형식의 스타일을 어레이타입으로 변형한다.
                        var view = this.item.elementView || this.item.relationView;
                        var style = [];
                        if (view.style) {
                            var itemStyle = JSON.parse(view.style);
                            if (!$.isEmptyObject(itemStyle)) {
                                for (var key in itemStyle) {
                                    style.push({
                                        key: key,
                                        value: itemStyle[key]
                                    });
                                }
                            }
                            this.style = style;
                        }

                        if (this.item.tracingTag) {
                            this.tracingTag = this.item.tracingTag;
                        }

                        //bpmnVue 에 프로퍼티 에디팅중임을 알린다.
                        //프로퍼티 에디팅 중 데피니션 변화는 히스토리에 기록된다.
                        this.bpmnVue.propertyEditing = true;
                        this.$emit('update:drawer', true);

                        // this.toggleRightSidenav();
                    } else {
                        //프로퍼티 에디팅 해제.
                        if (this.bpmnVue) {
                            this.bpmnVue.propertyEditing = false;
                        }
                        this.$emit('update:drawer', false);

                        // this.closeRightSidenav();
                    }
                }
            },
            x: function (val) {
                this._item.elementView.x = val;
                this.$emit('update:item', this._item);
            },
            y: function (val) {
                this._item.elementView.y = val;
                this.$emit('update:item', this._item);
            },
            width: function (val) {
                this._item.elementView.width = val;
                this.$emit('update:item', this._item);
            },
            height: function (val) {
                this._item.elementView.height = val;
                this.$emit('update:item', this._item);
            },
            style: {
                handler: function (newVal, oldVal) {
                    var style = {};
                    if (newVal && newVal.length) {
                        $.each(newVal, function (i, item) {
                            style[item.key] = item.value;
                        });
                    }
                    var view = this._item.elementView || this._item.relationView;
                    view.style = JSON.stringify(style);
                    this.$emit('update:item', this._item);
                },
                deep: true
            },
            //모델러에 의해 tracingTag 가 변경되었을 경우.
            tracingTag: function (value) {
                var me = this;
                //동일함.
                if (me._item.tracingTag == value) {
                    
                }
                //이미 있음.
                else if (me.bpmnVue.checkExistTracingTag(value)) {
                    console.log('TracingTag aleardy exist.');
                }
                //트레이싱 태그 값이 바뀜.
                else if (value && value.length > 0) {
                    var oldTracingTag = me._item.tracingTag;

                    //해당 액티비티 업데이트.
                    me._item.tracingTag = value;
                    me.$emit('update:item', me._item);

                    //해당 트레이싱 태그를 사용중인 릴레이션의 source,target 을 변경한다.
                    var sequenceFlows = me.bpmnVue.data.definition.sequenceFlows;
                    if (sequenceFlows && sequenceFlows.length) {
                        $.each(sequenceFlows, function (i, relation) {
                            if (relation.sourceRef == oldTracingTag) {
                                relation.sourceRef = value;
                            }
                            if (relation.targetRef == oldTracingTag) {
                                relation.targetRef = value;
                            }
                        });
                    }
                }
            }
        },
        methods: {
            open(ref) {
                this.navigationDrawer = true;
                console.log('Opened: ' + ref);
            },
            closePanel() {
                this.navigationDrawer = false
                this.$emit('close')
            },
            // close(ref) {
            //     this.navigationDrawer = false;
            //     console.log('Closed: ' + ref);
            // },
            // closeRightSidenav() {
            //     this.$refs.rightSidenav.close();
            // },
            // toggleRightSidenav() {
            //     this.$refs.rightSidenav.toggle();
            // },
            uuid: function () {
                function s4() {
                    return Math.floor((1 + Math.random()) * 0x10000)
                        .toString(16)
                        .substring(1);
                }

              return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                    s4() + '-' + s4() + s4() + s4();
            }
        }
    }
</script>


<style lang="scss" rel="stylesheet/scss">
    .md-sidenav.md-right .md-sidenav-content {
        width: 600px;
    }

</style>

