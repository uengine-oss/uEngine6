<template>
    <v-dialog
        md-open-from="#userPicker"
        md-close-to="#userPicker"
        ref="userPicker"
        v-model="openDialog"
        max-width="20%"
    >
        <v-card>
            <v-card-title>Role Mappings</v-card-title>
            <v-card-text>
                <div v-for="(role,index) in roles" :key="role.name">
                    <label>{{role.name}}</label>
                    <v-text-field
                        v-model="users[role.name]"
                    ></v-text-field>
                </div>
            </v-card-text>

            <v-card-actions>
                <v-btn class="primary" @click="closeUserPicker">Close</v-btn>
                <v-btn class="primary" @click="confirmUser">Confirm</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        props: {
            roles: Array,
            id: String
        },

        created: function () {
            this.loadUsers()
        },

        data: function () {
            return {
                users: {},
                openDialog:false
            };
        },
        mounted: function () {
            var me = this;
        },
        methods: {
            closeUserPicker(ref) {
                // this.$refs['userPicker'].close();
                this.openDialog = false;
            },
            openUserPicker(ref) {
                this.loadUsers();
                // this.$refs['userPicker'].open();
                this.openDialog = true;
            },
            confirmUser: function () {
                var me = this;
                $.each(me.roles, function (index, arg) {
                    if (me.users[arg.name] != undefined) {
                        // me.$http.get(`http://bpm.uengine.io:8088/instance/${me.id}/role-mapping/${arg.name}`)
                        me.$http.get(`${window.origin}/instance/${me.id}/role-mapping/${arg.name}`)
                        // var data = {_type: "org.uengine.kernel.RoleMapping", endpoint: me.users[arg.name]};
                        .then(function (response) {
                            //me.users[arg.name] = response.data.endpoint;  ---- X
                            Vue.set(me.users, arg.name, response.data.endpoint) //----O
                        })
                    }
                })
                this.$refs['userPicker'].close();
            },

            loadUsers: function () {
                var me = this;
                $.each(me.roles, function (index, arg) {
                    // me.$http.get(`http://bpm.uengine.io:8088/instance/${me.id}/role-mapping/${arg.name}`)
                    me.$http.get(`${window.origin}/instance/${me.id}/role-mapping/${arg.name}`)
                    // me.$root.codi('instance{/id}/role-mapping{/roleName}').get({id: me.id, roleName: arg.name})
                        .then(function (response) {
                            //me.users[arg.name] = response.data.endpoint;  ---- X
                            Vue.set(me.users, arg.name, response.data.endpoint) //----O
                        })
                })
            }
        }
    }
</script>

