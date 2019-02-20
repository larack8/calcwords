var e = Object.assign || function(e) {
    for (var n = 1; n < arguments.length; n++) {
        var i = arguments[n];
        for (var o in i) Object.prototype.hasOwnProperty.call(i, o) && (e[o] = i[o]);
    }
    return e;
}, n = require("./config"), i = require("./modules/@tencent/mini-wxgamelib/js/common/report_14359.js"), o = require("./modules/@tencent/mini-wxgamelib/js/common/bj_report.js"), t = require("./modules/@tencent/mini-wxgamelib/js/common/event.js");

o.init({
    delay: 2e3,
    from: "mmsapp/gzcjzc"
}), App({
    nilReport: [],
    pageInfo: {},
    event: new t(),
    onLaunch: function(e) {
        console.log("app", this), this.pageInfo = e || {}, require("./modules/@tencent/mini-wxgamelib/js/common/welogin.js").prepare(this, n);
    },
    onShow: function(e) {
        this.pageInfo = e || this.pageInfo || {};
    },
    onError: function(e) {
        o.report(e);
    },
    jumpToUrl: function(e, n) {
        "function" == typeof wx.openUrl ? (e = -1 != (e = e.replace(/(\?|\&)ssid=([^&]+)&?/i, "$1").replace(/&$/i, "")).indexOf("?") ? e.replace("?", "?isFromWeappEntry=1&ssid=29&") : -1 != e.indexOf("#") ? e.replace("#", "?isFromWeappEntry=1&ssid=29#") : e += "?ssid=29", 
        console.log("openUrl", e), wx.openUrl({
            url: e,
            complete: function(e) {
                "function" == typeof n && n(e);
            }
        })) : wx.hideToast();
    },
    delayReport: function() {
        var e = this;
        e.session(function(n) {
            (e.nilReport || []).length > 0 && (n.userId && "nil" != n.userId ? i.batchCltStat(n, e.nilReport.splice(0, e.nilReport.length)) : setTimeout(function() {
                e.delayReport();
            }, 1e3));
        });
    },
    report: function(o, t, r, s, p) {
        var c = arguments.length > 5 && void 0 !== arguments[5] ? arguments[5] : {}, l = this;
        l.pageInfo = l.pageInfo || {}, l.session(function(a) {
            var u = l.nilReport || [], d = {
                sAppid: n.appid,
                sGameId: n.game_appid,
                iSceneId: n.sceneid || 0,
                iUIArea: o,
                iActionId: r,
                iPositionId: t,
                iSourceID: 1108,
                iSsid: l.pageInfo.ssid || 0,
                sGeneralId: p || "0",
                sExternInfo: JSON.stringify(s) || ""
            };
            if (Object.assign(d, e({}, c)), !a.userId || "nil" == a.userId) return l.nilReport.push(d), 
            void setTimeout(function() {
                l.delayReport();
            }, 1e3);
            u = l.nilReport.splice(0, l.nilReport.length).concat(d), i.batchCltStat(a, u);
        });
    },
    debug: n.debug,
    mock: n.mock,
    config: n
});