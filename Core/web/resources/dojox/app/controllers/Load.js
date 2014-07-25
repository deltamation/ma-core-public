//>>built
define("dojox/app/controllers/Load","require dojo/_base/lang dojo/_base/declare dojo/on dojo/Deferred dojo/when ../Controller".split(" "),function(n,h,p,r,k,g,q,s){return p("dojox.app.controllers.Load",q,{_waitingQueue:[],constructor:function(b,a){this.events={"app-init":this.init,"app-load":this.load}},init:function(b){g(this.createView(b.parent,null,null,{templateString:b.templateString,controller:b.controller},null,b.type),function(a){g(a.start(),b.callback)})},load:function(b){this.app.log("in app/controllers/Load event.viewId\x3d"+
b.viewId+" event \x3d",b);for(var a=[],c=(b.viewId||"").split("+");0<c.length;){var e=c.shift();a.push(e)}var f;this.proceedLoadViewDef=new k;if(a&&1<a.length){for(var d=0;d<a.length-1;d++)c=h.clone(b),c.callback=null,c.viewId=a[d],this._waitingQueue.push(c);this.proceedLoadView(this._waitingQueue.shift());g(this.proceedLoadViewDef,h.hitch(this,function(){var c=h.clone(b);c.viewId=a[d];return f=this.loadView(c)}))}else return f=this.loadView(b)},proceedLoadView:function(b){var a=this.loadView(b);
g(a,h.hitch(this,function(){this.app.log("in app/controllers/Load proceedLoadView back from loadView for event",b);var a=this._waitingQueue.shift();a?(this.app.log("in app/controllers/Load proceedLoadView back from loadView calling this.proceedLoadView(nextEvt) for ",a),this.proceedLoadView(a)):(this._waitingQueue=[],this.proceedLoadViewDef.resolve())}))},loadView:function(b){var a=b.parent||this.app,c=(b.viewId||"").split(","),e=c.shift(),c=c.join(","),a=this.loadChild(a,e,c,b.params||"");b.callback&&
g(a,b.callback);return a},createChild:function(b,a,c,e){var f=b.id+"_"+a;!e&&b.views[a].defaultParams&&(e=b.views[a].defaultParams);if(c=b.children[f])return e&&(c.params=e),this.app.log("in app/controllers/Load createChild view is already loaded so return the loaded view with the new parms ",c),c;var d=new k;g(this.createView(b,f,a,null,e,b.views[a].type),function(a){b.children[f]=a;g(a.start(),function(a){d.resolve(a)})});return d},createView:function(b,a,c,e,f,d){var g=new k,m=this.app;n([d?d:
"../View"],function(d){d=new d(h.mixin({app:m,id:a,name:c,parent:b},{params:f},e));g.resolve(d)});return g},loadChild:function(b,a,c,e){if(!b)throw Error("No parent for Child '"+a+"'.");if(!a){var f=b.defaultView?b.defaultView.split(","):"default";a=f.shift();c=f.join(",")}var d=new k,l;try{l=this.createChild(b,a,c,e)}catch(m){return d.reject("load child '"+a+"' error."),d.promise}g(l,h.hitch(this,function(b){!c&&b.defaultView&&(c=b.defaultView);var f=c.split(",");a=f.shift();c=f.join(",");a?(b=this.loadChild(b,
a,c,e),g(b,function(){d.resolve()},function(){d.reject("load child '"+a+"' error.")})):d.resolve()}),function(){d.reject("load child '"+a+"' error.")});return d.promise}})});
//@ sourceMappingURL=Load.js.map