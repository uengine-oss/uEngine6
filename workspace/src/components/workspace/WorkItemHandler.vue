<template>
  <div v-if="workItem && selectWorkItem">
    <div style="width: 100%">
      <!-- <v-progress-circular
        v-if="test"
        indeterminate
        color="primary"
      ></v-progress-circular> -->
      <v-layout>
        <v-layout md-flex="40">
          <div>
            <v-layout>
<!--              <iam-avatar :workItem="workItem"></iam-avatar>-->
              <keycloak-avatar :workItem="workItem"></keycloak-avatar>
                <v-btn icon @click="unselectWorkItem()"><v-icon>mdi-arrow-left</v-icon></v-btn><br>
              <div style="margin-left: 16px">
                <span class="md-title">{{workItem.activity.name}}</span><br>
                <span class="md-caption">{{workItem.activity.description || 'No description for this activity.'}}</span>
              </div>
            </v-layout>
          </div>
          <!-- <v-divider style="width: 100%"></v-divider> -->
          <div>

          </div>
        </v-layout>
        <v-layout md-flex="60" md-align="end">
          <div>
            <v-layout v-if="workItem.worklist.status != 'COMPLETED'" class="bar-wrapper">

              <v-btn class="md-raised" text v-on:click="complete('COMPLETED')">완료</v-btn>

              <v-btn class="md-raised" text v-on:click="complete('SAVED')">저장</v-btn>
              <v-btn class="md-raised" text>건너뛰기</v-btn>
            </v-layout>

          </div>
          <div style="margin-left: 16px;">
            <v-layout class="bar-wrapper">
              <v-btn style="margin-right: 0px"
                         class="md-raised" text :color="btnColor.todo" v-bind:class="{ 'md-primary': menu == 'todo' }"
                         v-on:click="menu = 'todo'">
                <v-tooltip md-direction="bottom">Todo</v-tooltip>
                Todo
              </v-btn>
              <v-btn style="margin-left: 0px"
                         class="md-raised" text :color="btnColor.info" v-bind:class="{ 'md-primary': menu == 'info' }"
                         v-on:click="menu = 'info'">
                <v-tooltip md-direction="bottom">Info</v-tooltip>
                Info
              </v-btn>
            </v-layout>
          </div>
        </v-layout>
      </v-layout>
    </div>

    <br><br>

    <div style="width: 100%;">
      <v-layout v-if="menu == 'todo'" class="bg-white">
        <div class="header-top-line"></div>
        <v-card style="width: 100%;">
            <v-card v-if="workItem.worklist.status != 'COMPLETED'" >
              {{workItem.activity.description ? workItem.activity.description.text : ""}}
              <div v-if="parameterValueDefinition" style="width: 100%">
                <object-form
                  :classDefinition="parameterValueDefinition"
                  :value="workItem.parameterValues"
                  :metadataResolver="metadataResolver"
                ></object-form>
              </div>
            </v-card>
            <v-card v-else>
             Work-item has been processed.
            </v-card>
        </v-card>

      </v-layout>

      <v-layout v-if="menu == 'info'" class="bg-white">
        <div class="header-top-line"></div>
        <v-card style="width: 100%;">
            <v-card>
              <div style="height:600px;width: 100%;position: relative">
                <process-designer
                  :backend="backend"
                  :instanceId="''+workItem.worklist.instId"
                  :monitor="true"
                  :rootInstanceId="''+workItem.worklist.instId">
                </process-designer>
              </div>
            </v-card>
        </v-card>
      </v-layout>
    </div>
  </div>

</template>


<script>
  export default {
    props: {
      id: String,
      selectWorkItem: Boolean
    },
    watch: {
      id: function () {
        this.load();
      }
    },
    data: function () {
      return {
        menu: 'todo', //info
        parameterValueDefinition: null,
        workItem: null,
        classDiagram: null,
        backend: window.backend,
        path: null,
        test: false,
        btnColor:{
          todo: '#5572FA',
          info: null
        }
      };
    },
    mounted: function () {
      this.load();
    },
    watch:{
      menu(val){
        if(val == 'info'){
          this.btnColor.info = '#5572FA'
          this.btnColor.todo = null
        } else {
          this.btnColor.info = null
          this.btnColor.todo = '#5572FA'
        }
      }
    },

    methods: {
      load: function () {
        var me = this;
        me.test = true
        this.$EventBus.$on("setPath", function (data) {
          me.path = data
          me.workItem = null;
          var path = me.path
          // async selectedWorkItem(work){
            // var me = this
          var axios = require("axios");
          if(path){

            axios.get(path).then(function (workItem) {
            // },
            // window.backend.$bind('worklist/' + this.id, worklist);
            // worklist.$load().then(function (worklist) {
            //     worklist.workItem.$load().then(function (workItem) {
                  me.workItem = workItem.data;
                  if (!me.workItem.parameterValues)
                    me.workItem.parameterValues = {};
    
                  var parameterValueDefinition = {fieldDescriptors: []};
    
                  if (me.workItem.activity.parameters) {
                    for (var idx in me.workItem.activity.parameters) { //convert parameter map to type metadata for object-form
                      var parameter = me.workItem.activity.parameters[idx];
    
                      if (parameter.argument && parameter.argument.text && parameter.variable) {
    
                        if ("REST" == parameter.variable.persistOption) {
                          if (me.workItem.parameterValues[parameter.argument.text] && me.workItem.parameterValues[parameter.argument.text].uri) {
                            var uri = me.workItem.parameterValues[parameter.argument.text].uri;
    
                            var getLocation = function (href) {
                              var l = document.createElement("a");
                              l.href = href;
                              return l;
                            };
                            var l = getLocation(uri);
    
                            var externalUri;
                            if (l.hostname.indexOf(".") > -1) { //it is full url
                              externalUri = l.pathname;
                            } else {
                              var serviceName = l.hostname;
                              externalUri = serviceName + l.pathname;
                            }
    
    
                            var xhr = new XMLHttpRequest()
                            // var self = this;
                            xhr.open('GET', window.backend.$bind.ref + "/" + externalUri, false);
                            xhr.setRequestHeader("access_token", localStorage['access_token']);
                            xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
                            xhr.onload = function () {
                              var received = JSON.parse(xhr.responseText);
                              me.workItem.parameterValues[parameter.argument.text] = received;
    
                              var metadata = me.metadataResolver(parameter.variable.typeClassName)
    
                              if (metadata.fieldDescriptors)
                                metadata.fieldDescriptors.forEach(function (fd) {
                                  if (fd.attributes && fd.attributes['relationAnnotation']) {
                                    xhr.open('GET', received._links[fd.name].href, false);
                                    xhr.onload = function () {
                                      var items = JSON.parse(xhr.responseText);
                                      received[fd.name] = items;
                                    }
                                    xhr.send();
                                  }
                                })
    
                            }
                            xhr.send();
                          }
                        }
    
                        if (!parameter.variable.typeClassName) //TODO: it looks hard code
                          parameter.variable.typeClassName = "java.lang.String";
    
                        if (parameter.multipleInput) {
    
                          parameterValueDefinition.fieldDescriptors.push({
                            name: parameter.argument.text,
                            displayName: parameter.argument.text,
                            collectionClass: parameter.variable.typeClassName,
                            _online: "REST" == parameter.variable.persistOption
                          });
                        } else {
    
                          parameterValueDefinition.fieldDescriptors.push({
                            name: parameter.argument.text,
                            displayName: parameter.argument.text,
                            className: parameter.variable.name.indexOf("[roles].") == 0 ? "org.uengine.kernel.RoleMapping" : parameter.variable.typeClassName,
                            _online: "REST" == parameter.variable.persistOption
                          });
                        }
                      }
                    }
                    me.parameterValueDefinition = parameterValueDefinition;
                  }
                // }, function () {
                //   me.$root.$children[0].error('워크아이템을 불러올 수 없습니다.');
                // })
              }
            )
          }
        })
        me.test = false
      },
      unselectWorkItem(){
        var me = this
        me.$EventBus.$emit("unselectWorkItem", true)
        me.menu = 'todo'
      },
      complete: function (desiredState) {
        var serviceLocator = this.$root.$children[0].$refs["backend"]; //TODO hardcoded
        var me = this;
        if (me.parameterValueDefinition && me.parameterValueDefinition.fieldDescriptors) {
          me.parameterValueDefinition.fieldDescriptors.forEach(function (fieldDescriptor) {
            if (fieldDescriptor._online) { //REST persisted data only carries it's uri
              me.workItem.parameterValues[fieldDescriptor.name] = {
                _type: 'org.uengine.five.overriding.RestResourceProcessVariableValue',
                uri: me.workItem.parameterValues[fieldDescriptor.name]._links.self.href
              }
            }
          })
        }
        serviceLocator.invoke({
            //aaa
          path: me.path.replace('http://bpm.uengine.io:8088/', ''),
          method: 'POST',
          data: {
            worklist: {
              status: desiredState // 'COMPLETED' or 'SAVED'
            },
            parameterValues: this.workItem.parameterValues
          },
          success: function () {
            me.load();
            // me.$root.$children[0].success('작업을 완료했습니다.');
            me.$emit('update:reload', true);

            window.location.reload();
          },
          fail: function (value) {
            me.$root.$children[0].error('Fail to complete due to ' + value);
          }
        });
      },

      metadataResolver: function (className) {

        // var classDiagram;
        var classNameOnly = className;
        var definitionName;

        if (className.indexOf("#") > 0) {
          var definitionAndClassName = className.split("#");
          definitionName = definitionAndClassName[0];
          classNameOnly = definitionAndClassName[1];

          if (this.classDiagram == null || this.classDiagram.name != definitionName) { //find the class from the current class diagram

            var result;
            var uri = (encodeURI(window.backend.$bind.ref + "/definition/raw/" + definitionName + ".ClassDiagram.json"))

            console.log("try to get class diagram: " + uri);

            var xhr = new XMLHttpRequest();
            // var me = this;

            xhr.open('GET', uri, false);
            xhr.setRequestHeader("access_token", localStorage['access_token']);
            xhr.onload = function () {
              result = JSON.parse(xhr.responseText)
            };
            xhr.send();

            this.classDiagram = result.definition;

          }

        }


        if (this.classDiagram != null) {
          //trim the class name if collection
          try {
            var listExp = "List<"
            if (classNameOnly.indexOf(listExp) > -1)
              classNameOnly = classNameOnly.substring(listExp.length, classNameOnly.length - 1);
          } catch (e) {
            console.log(e);
          }
          for (var i in this.classDiagram.classDefinitions[1]) {
            var classDefinition = this.classDiagram.classDefinitions[1][i];
            if (classDefinition.name == classNameOnly) {
              classDefinition.baseUri = "backend-" + (this.classDiagram.name.replace(' ', '-') + "/" + classNameOnly + "s").toLowerCase();
              return classDefinition
            }
          }
        }
        throw "There's no Class Declaration for " + className;
      }

    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .bar-wrapper {
    .md-button {
      margin-left: 0px;
      margin-right: 0px;
    }
  }
</style>

