<template>
  <div>
    <router-view />
    <div v-if="profile == 'local'">
        <service-locator :host="'http://bpm.uengine.io:9090'" path="/"
                        resource-name="codi"></service-locator>

        <service-locator ref="backend" :host="'http://bpm.uengine.io:9090'"></service-locator>
      </div>
      <div v-else>
        <service-locator :host="'http://bpm.uengine.io:9090'" path="/"
                        resource-name="codi"></service-locator>

        <service-locator ref="backend"
                        :host="'http://bpm.uengine.io:9090'"></service-locator>
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


