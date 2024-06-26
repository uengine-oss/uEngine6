module.exports = function (keycloak) {
  let module = {};

  module.keycloak = keycloak;

  module.requireGuest = function (to, from, next) {
    // will stop the routing if isAuthenticated
    module.isAuthenticated(function (result) {
      next(!result);
    })
  };

  module.requireUser = function (to, from, next) {
    //isAuthenticated ? continue route
    //if not ? will login and come back
    module.isAuthenticated(function (result) {
      if (result) {
        next(result);
      } else {
        next({
          path: '/auth/login',
          query: {
            redirect: to.fullPath
          }
        })
      }
    });
  };
  module.isAuthenticated = function (callback) {

    // comment here if you have proper IAM server.
    callback(true);
    return;
    // comment end

    if (!localStorage['accessToken']) {
      callback(false);
    }
    module.keycloak.validateToken(localStorage['accessToken'])
      .done(function (info) {
        localStorage['user'] = info.context.user;
        localStorage['userName'] = info.context['userName'];
        localStorage['acl'] = info.context.user['metaData'].acl;
        localStorage['gitlab-id'] = info.context.user['metaData']['gitlab-id'];
        callback(true);
      })
      .fail(function () {
        callback(false);
      });
  };

  return module;
};

// module.exports = function (iam) {
//   let module = {};
//
//   module.iam = iam;
//
//   module.requireGuest = function (to, from, next) {
//     // will stop the routing if isAuthenticated
//     module.isAuthenticated(function (result) {
//       next(!result);
//     })
//   };
//
//   module.requireUser = function (to, from, next) {
//     //isAuthenticated ? continue route
//     //if not ? will login and come back
//     module.isAuthenticated(function (result) {
//       if (result) {
//         next(result);
//       } else {
//         next({
//           path: '/auth/login',
//           query: {
//             redirect: to.fullPath
//           }
//         })
//       }
//     });
//   };
//   module.isAuthenticated = function (callback) {
//
//     // comment here if you have proper IAM server.
//    callback(true);
//    return;
//     // comment end
//
//     if (!localStorage['access_token']) {
//       callback(false);
//     }
//     module.iam.validateToken(localStorage['access_token'])
//       .done(function (info) {
//         localStorage['user'] = info.context.user;
//         localStorage['userName'] = info.context['userName'];
//         localStorage['acl'] = info.context.user['metaData'].acl;
//         localStorage['gitlab-id'] = info.context.user['metaData']['gitlab-id'];
//         callback(true);
//       })
//       .fail(function () {
//         callback(false);
//       });
//   };
//
//   return module;
// };
