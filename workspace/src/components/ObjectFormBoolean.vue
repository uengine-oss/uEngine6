<template>

    <v-switch v-model="value" @change="change">Yes</v-switch>

</template>

<script>

  export default {
    name: 'object-form-boolean',

    created(){
      if(!this.value){
        this.value = false;
      }

      this.$emit("input", this.value);
    },

    props: {
      fieldDescriptor: Object,
      value: Object
    },

    methods: {

      change(){

        this.$emit("input", this.value);

      }
    }
  }

</script>

