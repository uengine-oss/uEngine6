/**
 *
 */
var KEYCLOAK = function (host, contextPath) {
  this.host = host;
  this.contextPath = contextPath;
  if (!host) {
    this.baseUrl = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');
  } else {
    this.baseUrl = this.host;
  }
  if (this.contextPath) {
    this.baseUrl = this.baseUrl + this.contextPath;
  }
  this.user = undefined;
  this.scopes = undefined;

  $(window).ajaxSend(function (e, xhr, options) {
    var token = localStorage.getItem('accessToken');
    var clientKey = localStorage.getItem('uengine-keycloack-client-key');
    var clientSecret = localStorage.getItem('uengine-keycloack-client-secret');
    xhr.setRequestHeader('Authorization', token);
    if (clientKey && clientSecret) {
      xhr.setRequestHeader('client-key', clientKey);
      xhr.setRequestHeader('client-secret', clientSecret);
    }
  });
};
KEYCLOAK.prototype = {
  adminLogout: function () {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('uengine-keycloack-client-key');
    localStorage.removeItem('uengine-keycloack-client-secret');
  },
  logout: function () {
    var options = {
      type: "POST",
      url: '/auth/realms/uEngine5-bpm/protocol/openid-connect/logout?redirect_uri=/',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Access-Control-Allow-Origin': '*',
        "Access-Control-Allow-Methods" : "GET,POST,PUT,DELETE,OPTIONS",
        "Access-Control-Allow-Headers": "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With"
      },
      contentType: "application/json",
      dataType: 'json',
      resolve: function (response) {
        localStorage.removeItem('accessToken');
        window.location = '/'
      },
      reject: function () {

      }
    };

    this.user = undefined;
    this.scopes = undefined;
    // localStorage.removeItem('accessToken');

    return this.send(options);
  },
  setDefaultClient: function (key, secret) {
    localStorage.setItem('uengine-keycloack-client-key', key);
    localStorage.setItem('uengine-keycloack-client-secret', secret);
  },
  adminLogin: function (username, password) {
    if (username) {
      localStorage.setItem('uengine-keycloack-client-key', username);
    }
    if (password) {
      localStorage.setItem('uengine-keycloack-client-secret', password);
    }
    var options = {
      type: "GET",
      url: '/rest/v1/security',
      dataType: "json",
      async: false
    };
    return this.send(options);
  },
  system: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/system',
      dataType: "json"
    };
    return this.send(options);
  },
  passwordCredentialsLogin: function (username, password, scope, token_type, claim) {
    var me = this;
    var data = {
      username: username,
      password: password,
      scope: scope,
      token_type: token_type,
      claim: claim,
      // client_id: localStorage.getItem('uengine-keycloack-client-key'),
      // client_secret: localStorage.getItem('uengine-keycloack-client-secret'),
      grant_type: 'password',

      // username: "user01",
      // password: "123",
      // scope: scope,
      // token_type: token_type,
      // claim: claim,
      client_id: "uEngine5-bpm",
      client_secret: "848479eb-6a43-41e9-a149-41a89634e7bd",
      // grant_type: 'password'
    };
    for (var key in data) {
      if (!data[key]) {
        delete data[key];
      }
    }
    var query_string = $.param(data);
    var options = {
      type: "POST",
      url: '/auth/realms/uEngine5-bpm/protocol/openid-connect/token',
      data: query_string,
      contentType: "application/x-www-form-urlencoded",
      dataType: 'json',
      resolve: function (response) {
        if (response['access_token']) {
          var token = response['access_token'];
          console.log('TOKEN!!!', token)
          localStorage.setItem("accessToken", token);
          me.user = undefined;
          me.scopes = undefined;
          return response;
        } else {
          console.log('login failed');
          localStorage.removeItem("accessToken");
          me.user = undefined;
          me.scopes = undefined;
          return response;
        }
      }
    };
    return this.send(options);
  },
  validateToken: function (token) {
    var me = this;
    token = token ? token : localStorage.getItem("accessToken");

    var options = {
      type: "GET",
      url: '/auth/realms/uEngine5-bpm/protocol/openid-connect/userinfo',
      dataType: "json",
      token_type: "Bearer",
      access_token: token,
      async: true,
      resolve: function (response) {
        if (response['context']) {
          me.user = response['context'].user;
          me.scopes = response['context'].scopes;
          return response;
        } else {
          me.user = undefined;
          me.scopes = undefined;
          return response;
        }
      }
    };
    return this.send(options);
  },
  getUserInfoByToken() {
    var me = this
    var accessToken = localStorage.getItem('accessToken')
    if (accessToken == null) {
      return null;
    }
    if (window.Jwt.jws.JWS.parse(accessToken)) {
      return window.Jwt.jws.JWS.parse(accessToken).payloadObj
    }
    return null;
  },
  getUser: function (userName) {
    var me = this
    var accessToken = localStorage.getItem('accessToken')
    // if(accessToken){
    //   this.validateToken(accessToken)
    // }else{
    //
    // }

    // var options = {
    //   type: "GET",
    //   url: '/rest/v1//user/search/findByUserName?userName=' + userName,
    //   dataType: "json"
    // };

    var options = {
      type: "GET",
      url: "/auth/realms/uEngine5-bpm/protocol/openid-connect/userinfo",
      contentType: "application/json",
      token_type: "Bearer",
      access_token: accessToken,
      dataType: "text",
      resolve: function (response) {
        if (response['access_token']) {
          var token = response['access_token'];
          console.log('TOKEN!!!', token)
          localStorage.setItem("accessToken", token);
          me.user = undefined;
          me.scopes = undefined;
          return response;
        } else {
          console.log('login failed');
          localStorage.removeItem("accessToken");
          me.user = undefined;
          me.scopes = undefined;
          return response;
        }
      }
    };

    // http://localhost:8080/auth/realms/uEngine5-bpm/protocol/openid-connect/userinfo
    return this.send(options);
  },
  getUserSearch: function (userName, page, size) {
    var data = {
      userName: userName,
      page: page,
      size: size
    };
    console.log('getUserSearch', data);
    var url = userName ? '/rest/v1/user/search/findLikeUserName' : '/rest/v1/user';
    var options = {
      type: "GET",
      url: url,
      dataType: 'json',
      data: data,
      resolve: function (response, status, xhr) {
        var total = parseInt(xhr.getResponseHeader('x-uengine-pagination-totalnbrecords'));
        var offset = parseInt(xhr.getResponseHeader('x-uengine-pagination-currentoffset'));
        console.log(total, offset);
        return {
          data: response,
          total: total,
          offset: offset,
          page: page,
          size: size
        };
      }
    };
    return this.send(options);
  },
  createUser: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/user',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  updateUser: function (userName, data) {
    data['userName'] = userName;
    var options = {
      type: "PUT",
      url: '/rest/v1/user',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteUser: function (userName) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/user?userName=' + userName
    };
    return this.send(options);
  },
  createUserAvatarByFormData: function (file, contentType, userName) {
    var formData = new FormData();
    formData.append('userName', userName);
    formData.append('contentType', contentType);
    formData.append('file', file);
    var options = {
      type: "POST",
      url: '/rest/v1/avatar/formdata',
      data: formData,
      contentType: false,
      processData: false,
      dataType: 'json'
    };
    return this.send(options);
  },
  deleteUserAvatar: function (userName) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/avatar?userName=' + userName
    };
    return this.send(options);
  },
  getUserAvatarUrl: function (userName) {
    return this.baseUrl + '/rest/v1/avatar?userName=' + userName;
  },
  signUp: function (redirect_url, userData, authorizeResponse) {
    var data = {
      redirect_url: redirect_url,
      oauthUser: userData,
      authorizeResponse: authorizeResponse
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  signUpAccept: function (token) {
    var data = {
      token: token
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  forgotPassword: function (redirect_url, userName, authorizeResponse) {
    var data = {
      redirect_url: redirect_url,
      oauthUser: {
        userName: userName
      },
      authorizeResponse: authorizeResponse
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  registTokenVerification: function (token) {
    var options = {
      type: "GET",
      url: '/rest/v1/user/verification?token=' + token,
      dataType: "json",
      async: false
    };
    return this.send(options);
  },
  forgotPasswordAccept: function (token, password) {
    var data = {
      token: token,
      password: password
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  getClient: function (clientKey) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientKey,
      dataType: "json"
    };
    return this.send(options);
  },
  getAllClient: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/client',
      dataType: "json"
    };
    return this.send(options);
  },
  getAllScope: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/scope',
      dataType: "json"
    };
    return this.send(options);
  },
  getScope: function (scopeName) {
    var options = {
      type: "GET",
      url: '/rest/v1/scope/' + scopeName,
      dataType: "json"
    };
    return this.send(options);
  },
  getAllTemplate: function (clientKey) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientKey + '/template',
      dataType: 'json'
    };
    return this.send(options);
  },
  send: function (options) {
    var caller = arguments.callee.caller.name;
    var me = this;
    var deferred = $.Deferred();
    var ajaxOptions = {
      type: options.type,
      url: me.baseUrl + options.url
    };
    if (options.processData || typeof options.processData == 'boolean') {
      ajaxOptions.processData = options.processData;
    }

    if (options.dataType) {
      ajaxOptions.dataType = options.dataType;
    }
    if (options.contentType || typeof options.contentType == 'boolean') {
      ajaxOptions.contentType = options.contentType;
    }
    if (typeof options.async == 'boolean' && !options.async) {
      ajaxOptions.async = false;
    }
    if (options.data) {
      ajaxOptions.data = options.data;
    }

    if (options.headers) {
      ajaxOptions.headers = {}
      ajaxOptions.headers = options.headers
    }


    var promise = $.ajax(ajaxOptions);
    promise.done(function (response, status, xhr) {
      console.log(caller + ' success');
      if (options.resolve) {
        response = options.resolve(response, status, xhr);
      }
      deferred.resolve(response);
    });
    promise.fail(function (response, status, errorThrown) {
      console.log(caller + ' failed');
      console.log(response, status, errorThrown);
      if (options.reject) {
        response = options.reject(response, status, errorThrown);
      }
      deferred.reject(response);
    });
    return deferred.promise();
  }
}
;
KEYCLOAK.prototype.constructor = KEYCLOAK;
