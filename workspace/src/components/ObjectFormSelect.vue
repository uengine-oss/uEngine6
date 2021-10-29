<template>
  <v-select>
    <v-option v-for="(value, index) in fieldDescriptor.values" value="value" :key="index">{{fieldDescriptor.options[index] ? fieldDescriptor.options[index] : value}}</v-option>
  </v-select>
</template>

<script>

  export default {
    name: 'object-form-select',

    props: {
      fieldDescriptor: Object
    }
  }

</script>

