(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-384bb30a"],{"07ae":function(e,t,s){"use strict";var i=s("845e"),n=s.n(i);n.a},"129f":function(e,t){e.exports=Object.is||function(e,t){return e===t?0!==e||1/e===1/t:e!=e&&t!=t}},"498a":function(e,t,s){"use strict";var i=s("23e7"),n=s("58a8").trim,a=s("c8d2");i({target:"String",proto:!0,forced:a("trim")},{trim:function(){return n(this)}})},"5eba":function(e,t,s){},"6ea6":function(e,t,s){"use strict";s.r(t);var i=function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("div",{staticStyle:{"max-width":"350px"}},["DESIGN"===e.mode?s("div",[s("el-button",{attrs:{disabled:"",icon:"iconfont icon-map-site",type:"primary",size:"mini",round:""}},[e._v(" 选择角色")]),s("span",{staticClass:"placeholder"},[e._v(" "+e._s(e.placeholder))])],1):s("div",[!e.formDisable||e._value.length<=0?s("div",[s("el-button",{attrs:{disabled:e.formDisable,icon:"iconfont icon-map-site",type:"primary",size:"mini",round:""},on:{click:function(t){return e.$refs.orgPicker.show()}}},[e._v(" 选择角色")]),s("org-picker",{ref:"orgPicker",attrs:{type:"role",multiple:e.multiple,selected:e._value},on:{ok:e.selected}}),s("span",{staticClass:"placeholder"},[e._v(" "+e._s(e.placeholder))])],1):e._e(),s("div",{staticStyle:{"margin-top":"5px"}},e._l(e._value,(function(t,i){return s("el-tag",{staticStyle:{margin:"5px"},attrs:{closable:!e.formDisable},on:{close:function(t){return e.delDept(i)}}},[e._v(e._s(t.name))])})),1)])])},n=[],a=(s("a434"),s("8f73")),c=s("709c"),r={mixins:[a["a"]],name:"RolePicker",components:{OrgPicker:c["a"]},props:{value:{type:Array,default:function(){return[]}},placeholder:{type:String,default:"请选择角色"},multiple:{type:Boolean,default:!1}},data:function(){return{showOrgSelect:!1}},methods:{selected:function(e){this.showOrgSelect=!1,this._value=e},delDept:function(e){this._value.splice(e,1)}}},l=r,o=(s("e6dd"),s("2877")),d=Object(o["a"])(l,i,n,!1,null,"02ada110",null);t["default"]=d.exports},"709c":function(e,t,s){"use strict";var i=function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("w-dialog",{attrs:{border:!1,closeFree:"",width:"600px",title:e._title},on:{ok:e.selectOk},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},[s("div",{staticClass:"picker"},[s("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"candidate"},["role"!==e.type?s("div",[s("el-input",{staticStyle:{width:"95%"},attrs:{size:"small",clearable:"",placeholder:"搜索","prefix-icon":"el-icon-search"},on:{input:e.searchUser},model:{value:e.search,callback:function(t){e.search=t},expression:"search"}}),s("div",{directives:[{name:"show",rawName:"v-show",value:!e.showUsers,expression:"!showUsers"}]},[s("ellipsis",{staticStyle:{height:"18px",color:"#8c8c8c",padding:"5px 0 0"},attrs:{hoverTip:"",row:1,content:e.deptStackStr}},[s("i",{staticClass:"el-icon-office-building",attrs:{slot:"pre"},slot:"pre"})]),s("div",{staticStyle:{"margin-top":"5px"}},[e.multiple?s("el-checkbox",{on:{change:e.handleCheckAllChange},model:{value:e.checkAll,callback:function(t){e.checkAll=t},expression:"checkAll"}},[e._v("全选")]):e._e(),s("span",{directives:[{name:"show",rawName:"v-show",value:e.deptStack.length>0,expression:"deptStack.length > 0"}],staticClass:"top-dept",on:{click:e.beforeNode}},[e._v("上一级")])],1)],1)],1):s("div",{staticClass:"role-header"},[s("div",[e._v("系统角色")])]),s("div",{staticClass:"org-items",style:"role"===e.type?"height: 350px":""},[s("el-empty",{directives:[{name:"show",rawName:"v-show",value:!e.nodes||0===e.nodes.length,expression:"!nodes || nodes.length === 0"}],attrs:{"image-size":100,description:"似乎没有数据"}}),e._l(e.nodes,(function(t,i){return s("div",{key:i,class:e.orgItemClass(t)},[t.type===e.type?s("el-checkbox",{on:{change:function(s){return e.selectChange(t)}},model:{value:t.selected,callback:function(s){e.$set(t,"selected",s)},expression:"org.selected"}}):e._e(),"dept"===t.type?s("div",{on:{click:function(s){return e.triggerCheckbox(t)}}},[s("i",{staticClass:"el-icon-folder-opened"}),s("span",{staticClass:"name",attrs:{title:t.name}},[e._v(e._s(t.name.substring(0,12)))]),s("span",{class:"next-dept"+(t.selected?"-disable":""),on:{click:function(s){s.stopPropagation(),!t.selected&&e.nextNode(t)}}},[s("i",{staticClass:"iconfont icon-map-site"}),e._v(" 下级 ")])]):"user"===t.type?s("div",{staticStyle:{display:"flex","align-items":"center"},on:{click:function(s){return e.triggerCheckbox(t)}}},[e.$isNotEmpty(t.avatar)?s("el-avatar",{attrs:{size:35,src:t.avatar}}):s("span",{staticClass:"avatar"},[e._v(e._s(e.getShortName(t.name)))]),s("span",{staticClass:"name",attrs:{title:t.name}},[e._v(e._s(t.name.substring(0,12)))])],1):s("div",{staticStyle:{display:"inline-block"},on:{click:function(s){return e.triggerCheckbox(t)}}},[s("i",{staticClass:"iconfont icon-bumen"}),s("span",{staticClass:"name",attrs:{title:t.name}},[e._v(e._s(t.name.substring(0,12)))])])],1)}))],2)]),s("div",{staticClass:"selected"},[s("div",{staticClass:"count"},[s("span",[e._v("已选 "+e._s(e.select.length)+" 项")]),s("span",{on:{click:e.clearSelected}},[e._v("清空")])]),s("div",{staticClass:"org-items",staticStyle:{height:"350px"}},[s("el-empty",{directives:[{name:"show",rawName:"v-show",value:0===e.select.length,expression:"select.length === 0"}],attrs:{"image-size":100,description:"请点击左侧列表选择数据"}}),e._l(e.select,(function(t,i){return s("div",{key:i,class:e.orgItemClass(t)},["dept"===t.type?s("div",[s("i",{staticClass:"el-icon-folder-opened"}),s("span",{staticClass:"name",staticStyle:{position:"static"}},[e._v(e._s(t.name))])]):"user"===t.type?s("div",{staticStyle:{display:"flex","align-items":"center"}},[e.$isNotEmpty(t.avatar)?s("el-avatar",{attrs:{size:35,src:t.avatar}}):s("span",{staticClass:"avatar"},[e._v(e._s(e.getShortName(t.name)))]),s("span",{staticClass:"name"},[e._v(e._s(t.name))])],1):s("div",[s("i",{staticClass:"iconfont icon-bumen"}),s("span",{staticClass:"name"},[e._v(e._s(t.name))])]),s("i",{staticClass:"el-icon-close",on:{click:function(t){return e.noSelected(i)}}})])}))],2)])])])},n=[],a=(s("4160"),s("d81d"),s("a434"),s("b0c0"),s("ac1f"),s("841c"),s("498a"),s("159b"),s("0c6d"));function c(e){return Object(a["a"])({url:"/oa/org/tree",method:"get",params:e})}function r(e){return Object(a["a"])({url:"/oa/org/tree/user",method:"get",params:e})}function l(e){return Object(a["a"])({url:"/oa/role",method:"get",params:e})}var o={name:"OrgPicker",components:{},props:{title:{default:"请选择",type:String},type:{type:String,required:!0},multiple:{default:!1,type:Boolean},selected:{default:function(){return[]},type:Array}},data:function(){return{visible:!1,loading:!1,checkAll:!1,nowDeptId:null,isIndeterminate:!1,searchUsers:[],nodes:[],select:[],search:"",deptStack:[]}},computed:{_title:function(){return"user"===this.type?"请选择用户"+(this.multiple?"[多选]":"[单选]"):"dept"===this.type?"请选择部门"+(this.multiple?"[多选]":"[单选]"):"role"===this.type?"请选择角色"+(this.multiple?"[多选]":"[单选]"):"-"},deptStackStr:function(){return String(this.deptStack.map((function(e){return e.name}))).replaceAll(","," > ")},showUsers:function(){return this.search||""!==this.search.trim()}},methods:{show:function(){this.visible=!0,this.init(),this.getDataList()},orgItemClass:function(e){return{"org-item":!0,"org-dept-item":"dept"===e.type,"org-user-item":"user"===e.type,"org-role-item":"role"===e.type}},getDataList:function(){var e=this;if(this.loading=!0,"user"===this.type)return r({deptId:this.nowDeptId,keywords:this.search}).then((function(t){e.loading=!1,e.nodes=t.data,e.selectToLeft()})),"请选择用户";"dept"===this.type?c({deptId:this.nowDeptId,keywords:this.search}).then((function(t){e.loading=!1,e.nodes=t.data,e.selectToLeft()})):"role"===this.type&&l({deptId:this.nowDeptId,keywords:this.search}).then((function(t){e.loading=!1,e.nodes=t.data,e.selectToLeft()}))},getShortName:function(e){return e?e.length>2?e.substring(1,3):e:"**"},searchUser:function(){},selectToLeft:function(){var e=this,t=""===this.search.trim()?this.nodes:this.searchUsers;t.forEach((function(t){for(var s=0;s<e.select.length;s++){if(e.select[s].id===t.id){t.selected=!0;break}t.selected=!1}}))},triggerCheckbox:function(e){e.type==this.type&&(e.selected=!e.selected,this.selectChange(e))},selectChange:function(e){if(e.selected)this.multiple||(this.nodes.forEach((function(e){e.selected=!1})),this.select=[]),e.selected=!0,this.select.push(e);else{this.checkAll=!1;for(var t=0;t<this.select.length;t++)if(this.select[t].id===e.id){this.select.splice(t,1);break}}},noSelected:function(e){for(var t=this.nodes,s=0;s<2;s++){for(var i=0;i<t.length;i++)if(t[i].id===this.select[e].id){t[i].selected=!1,this.checkAll=!1;break}t=this.searchUsers}this.select.splice(e,1)},handleCheckAllChange:function(){var e=this;this.nodes.forEach((function(t){if(e.checkAll)t.selected||t.type!=e.type||(t.selected=!0,e.select.push(t));else{t.selected=!1;for(var s=0;s<e.select.length;s++)if(e.select[s].id===t.id){e.select.splice(s,1);break}}}))},nextNode:function(e){this.nowDeptId=e.id,this.deptStack.push(e),this.getDataList()},beforeNode:function(){0!==this.deptStack.length&&(this.deptStack.length<2?this.nowDeptId=null:this.nowDeptId=this.deptStack[this.deptStack.length-2].id,this.deptStack.splice(this.deptStack.length-1,1),this.getDataList())},recover:function(){this.select=[],this.nodes.forEach((function(e){return e.selected=!1}))},selectOk:function(){this.$emit("ok",Object.assign([],this.select.map((function(e){return e.avatar=void 0,e})))),this.visible=!1,this.recover()},clearSelected:function(){var e=this;this.$confirm("您确定要清空已选中的项?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){e.recover()}))},close:function(){this.$emit("close"),this.recover()},init:function(){this.checkAll=!1,this.nowDeptId=null,this.deptStack=[],this.nodes=[],this.select=Object.assign([],this.selected),this.selectToLeft()}}},d=o,u=(s("07ae"),s("2877")),h=Object(u["a"])(d,i,n,!1,null,"35bed664",null);t["a"]=h.exports},"841c":function(e,t,s){"use strict";var i=s("d784"),n=s("825a"),a=s("1d80"),c=s("129f"),r=s("14c3");i("search",1,(function(e,t,s){return[function(t){var s=a(this),i=void 0==t?void 0:t[e];return void 0!==i?i.call(t,s):new RegExp(t)[e](String(s))},function(e){var i=s(t,e,this);if(i.done)return i.value;var a=n(e),l=String(this),o=a.lastIndex;c(o,0)||(a.lastIndex=0);var d=r(a,l);return c(a.lastIndex,o)||(a.lastIndex=o),null===d?-1:d.index}]}))},"845e":function(e,t,s){},"8f73":function(e,t,s){"use strict";t["a"]={props:{mode:{type:String,default:"DESIGN"},formDisable:{type:Boolean,default:!1},required:{type:Boolean,default:!1}},data:function(){return{}},watch:{_value:function(e,t){this.$emit("change",e)}},computed:{_value:{get:function(){return this.value},set:function(e){this.$emit("input",e)}}}}},c8d2:function(e,t,s){var i=s("d039"),n=s("5899"),a="​᠎";e.exports=function(e){return i((function(){return!!n[e]()||a[e]()!=a||n[e].name!==e}))}},d81d:function(e,t,s){"use strict";var i=s("23e7"),n=s("b727").map,a=s("1dde"),c=s("ae40"),r=a("map"),l=c("map");i({target:"Array",proto:!0,forced:!r||!l},{map:function(e){return n(this,e,arguments.length>1?arguments[1]:void 0)}})},e6dd:function(e,t,s){"use strict";var i=s("5eba"),n=s.n(i);n.a}}]);
//# sourceMappingURL=chunk-384bb30a.ea24de77.js.map