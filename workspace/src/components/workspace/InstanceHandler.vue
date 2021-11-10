<template>
    <v-card v-if="instance">
        <v-card-title style="position:absolute; left:120px; top:0; z-index:1; margin-top:-6px;">
            <div>{{instance.name}}</div>
            <div>Started by {{instance.initRsNm}} at {{instance.startedDate}}</div>
        </v-card-title>
        <v-card-text>
            <div style="height:600px;">
                <process-designer
                        :backend="backend"
                        :instanceId="'' + id"
                        :monitor="true"
                        :rootInstanceId="'' + instance.rootInstId">
                </process-designer>
            </div>
        </v-card-text>
    </v-card>
</template>

<!--<template>-->

<!--    <md-card md-with-hover v-if="instance">-->
<!--        <md-card-header>-->

<!--            <div class="md-title">{{instance.name}}</div>-->
<!--            <div class="md-subhead">Started by {{instance.initRsNm}} at {{instance.startedDate}}</div>-->
<!--        </md-card-header>-->

<!--        <md-card-content>-->
<!--            <md-tabs md-right :md-dynamic-height="false" class="md-transparent example-tabs" style="margin-top: -48px;"-->
<!--                     @change="onChangeTab">-->
<!--                <md-tab class="code-content" md-label="Info" id="info" md-active>-->

<!--                    <div style="height:1000px" v-if="infoEnabled">-->
<!--                        <process-designer-->
<!--                                :backend="backend"-->
<!--                                :instanceId="'' + id"-->
<!--                                :monitor="true"-->
<!--                                :rootInstanceId="'' + instance.rootInstId">-->
<!--                        </process-designer>-->
<!--                    </div>-->
<!--                </md-tab>-->
<!--                <md-tab class="example-content" md-label="Actions">-->

<!--                    <md-button v-for="(signalEvent, key) in variables[':signalEvents:prop']" @click="signal(key)">-->
<!--                        {{signalEvent.signalName}}-->
<!--                    </md-button>-->

<!--                </md-tab>-->
<!--            </md-tabs>-->
<!--        </md-card-content>-->
<!--    </md-card>-->
<!--</template>-->


<script>

    export default {
        props: {
            id: String,
        },
        watch: {
            id: function (val) {
                this.load();
            }
        },
        data: function () {
            return {
                instance: null,
                variables: {},
                // backend: window.backend,
                backend: null,
                infoEnabled: false,
            };
        },
        created() {
            var me = this
            console.log('CRETATE')
            // /**
            //  * Hybind
            //  */
            var hybind = require("hybind");
            var access_token = localStorage["access_token"];
            var profile = window.profile;

            var backend;
            if (profile == 'dev') {
                backend = hybind("http://bpm.uengine.io:8088", {headers: {'access_token': access_token}});
            } else {
                //  backend = hybind("http://" + config.vcap.services['uengine5-router'][profile].external, {headers: {'access_token': access_token}});
                backend = hybind("http://bpm.uengine.io", {headers: {'access_token': access_token}});
            }

            me.backend = backend;
            console.log(me.backend)

        },
        mounted: function () {
            console.log('MOUNTED')
            this.load();
        },

        methods: {
            load: function () {
                var me = this;
                var instance = {}
                me.backend.$bind('instances/' + me.id, instance);
                instance.$load().then(function (instance) {
                    me.instance = instance;
                    console.log(me.instance);
                    me.$http.get(`${window.origin}/instance/${me.id}/variables`)
                    // me.$root.codi('instance{/id}/variables').get({id: me.id})
                        .then(function (response) {
                            me.variables = response.data;
                        });
                });
            },

            onChangeTab: function (tabIndex) {
                if (tabIndex == 0) {
                    this.infoEnabled = true;
                } else {
                    this.infoEnabled = false;
                }
            },

            signal: function (key) {
                var me = this;
                // me.$http.get(`http://bpm.uengine.io:9090/instance/${me.id}/variables`)
                // me.$root.codi('instance/' + me.id + '/signal/' + key).save()
                //     .then(function (response) {
                //         alert('succeed');
                //     });
            }
        }
    }
</script>

