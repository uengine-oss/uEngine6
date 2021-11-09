<template>
  <v-container fluid class="cards-page mt-3">
    <v-row>
      <v-col cols=12>
        <v-card class="overflow-hidden mx-1 mb-1">
          <v-img
            :src="cards[0].src"
            class="white--text align-start"
            gradient="to top, rgba(0,0,0,.1), rgba(0,0,0,.5)"
            height="500">
            <v-card-text class="pa-5">
              <h5>Mega Process</h5>
              <h2>영업 기회 관리 프로세스</h2>
              <p>A lifestyle brand is a company that markets its products or services to embody the interests, attitudes,
                and opinions of a group or a culture. Lifestyle brands seek to inspire, guide, and motivate people, with
                the goal of their products contributing to the definition of the consumer's way of life.</p>
              <v-btn
                color="primary"
                dark
                class="button-shadow">
                get started
              </v-btn>
            </v-card-text>

          </v-img>
        </v-card>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" md="4" v-for="(defi, i) in DefinitionList" :key="i">
        <v-card class="overflow-hidden mx-1">
          <v-img
            src="https://user-images.githubusercontent.com/65217813/135188899-ace6be59-7af3-4814-8c26-dda65c28a243.png"
            class="white--text align-end"
            height="215"
          ></v-img>
          <v-card-text class="pa-5">
            <h3 class="greyBold--text">{{defi.name}}</h3>
            <p class="greyBold--text">{{defi.path}}</p>
          </v-card-text>
          <v-card-actions class="ma-2">
            <!-- <v-btn
              @click="editProcess(defi)"
              color="primary"
              class="button-shadow"
            >
              수정
            </v-btn>
            <v-btn
              color="primary"
              class="button-shadow"
            >
              이동
            </v-btn>
            <v-btn
              color="primary"
              class="button-shadow"
              @click="deleteDefinition(defi)"
            >
              삭제
            </v-btn> -->
            <v-btn
              @click="initiateProcess(defi)"
              color="error"
              class="button-shadow"
            >
              시작
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'Cards',
  props: {
    backend: Object,
    iam: Object,
    path: String
  },
  data () {
    return {
      menu: [
        'Edit',
        'Copy',
        'Delete',
        'Print'
      ],
      cards: [
        { title: 'Pre-fab homes', src: require('@/assets/img/cards/mac.png'), flex: 12 },
        { title: 'Lizard', src: require('@/assets/img/cards/candy.png'), flex: 6 },
        { title: 'Best airlines', src: require('@/assets/img/cards/city.png'), flex: 6 },
        { title: 'Best airlines', src: require('@/assets/img/cards/rns.png'), flex: 6 },
      ],
      DefinitionList: null,
      instanceId: null,
    }
  },
  mounted(){
    var me = this
    this.$http.get("http://bpm.uengine.io/definition").then((result)=>{
        me.DefinitionList = result.data._embedded.definitions
        console.log(me.DefinitionList)
    })
    this.$http.get("http://bpm.uengine.io/instances/search/findFilterICanSee").then((result2)=>{
        me.instanceId = result2.data._embedded.instances.length + 1
    })
  },
  methods: {
    initiateProcess: function (defi) {
      var me = this
      console.log(defi)
      var axios = require("axios");
        axios.post(defi._links.instantiation.href, {"simulation": false})
          .then(
            function (instance) {
              console.log(instance)
              var instanceId = instance.data.instanceId
              // me.$root.$children[0].success('프로세스가 시작되었습니다.');
              me.$router.push({
                path: '/instance/' + instanceId + '/' + instanceId
              })
            },
            function () {
              // me.$root.$children[0].error('프로세스를 시작할 수 없습니다.');
            }
          );
          // me.instanceId ++;
      },
      getDefinitionList: function () {
        var me = this;
        var definitions = [];
        var url = "definition";
        if (me.currentPath && me.currentPath.length) {
          url = url + '/' + me.currentPath;
        }
        me.backend.$bind(url, definitions);

        var cards = [];
        var folders = [];

        definitions.$load().then(function (definitions) {

          if (definitions) {
            definitions.forEach(function (definition) {
              if (definition.directory) {
                folders.push(definition);
              } else {
                //path
                definition.desc = name + '...';
                //localStorage['svg-' + me.id];
                var svgData = localStorage['svg-' + definition.path.replace('.xml', '').replace('.json', '')];
                if (svgData) {
                  //definition.src = 'data:image/svg+xml;utf-8,' + svgData;
                  definition.src = svgData;
                  me.backend.$bind("definition", definition);
                } else {
                  definition.src = '/static/image/sample.png';
                }
                cards.push(definition);
              }
            });
          }
        });

        //localStorage['svg-' + me.id] = svgData;
        me.directory = folders;
        me.cards = cards;
      },
  }
}
</script>

<style src="./Cards.scss" lang="scss"/>
