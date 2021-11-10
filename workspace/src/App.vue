<template>
  <div>
    <router-view />
    <div v-if="profile == 'dev'">
        <service-locator :host="'http://bpm.uengine.io:8088'" path="/"
                        resource-name="codi"></service-locator>

        <service-locator ref="backend" :host="'http://bpm.uengine.io:8088'"></service-locator>
      </div>
      <div v-else>
        <service-locator :host="'http://bpm.uengine.io'" path="/"
                        resource-name="codi"></service-locator>

        <service-locator ref="backend"
                        :host="'http://bpm.uengine.io'"></service-locator>
        <!-- <service-locator ref="backend"
                        :host="'http://' + config.vcap.services['uengine5-router'][profile].external"></service-locator> -->
      </div>
  </div>
</template>

<script>
  export default {
    name: 'App',
  };
</script>

<style src="./styles/app.scss" lang="scss"/>


