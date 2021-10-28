// module.exports = {
//   "transpileDependencies": [
//     "vuetify"
//   ],
//   publicPath: '/',
// }

// http
module.exports = {
    configureWebpack: {
        devServer: {
            host: 'localhost',
            port: '8083',
            disableHostCheck: true,
            overlay: false
        }
    },
    "transpileDependencies": [
        "vuetify"
    ],
    "runtimeCompiler": true
}