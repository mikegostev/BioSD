/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

(function($, undefined) {
    if($ == undefined)
        throw "jQuery not loaded";

    $.fn.extend({

        aePager: function() {
            return this.each(function() {
			new $.AEPager(this);
            });
        }
    });

    $.AEPager = function(elt) {

        var $element = $(elt);
        var curPage = $element.find("#page").first().text();
        var pageSize = $element.find("#page_size").first().text();
        var totalPages = $element.find("#total_pages").first().text();

        if ( totalPages > 1 ) {
            var pagerHtml = "Pages: ";
            for ( var page = 1; page <= totalPages; page++ ) {
                if ( curPage == page ) {
                    pagerHtml = pagerHtml + "<span id=\"current_page\">" + page + "</span>";
                } else if ( 2 == page && curPage > 6 && totalPages > 11 ) {
                    pagerHtml = pagerHtml + "..";
                } else if ( totalPages - 1 == page && totalPages - curPage > 5 && totalPages > 11 ) {
                    pagerHtml = pagerHtml + "..";
                } else if ( 1 == page || ( curPage < 7 && page < 11 ) || ( Math.abs( page - curPage ) < 5 ) || ( totalPages - curPage < 6 && totalPages - page < 10 ) || totalPages == page || totalPages <= 11 ) {
                    var newQuery = $.query.set( "page", page ).set( "pagesize", pageSize ).toString();
                    pagerHtml = pagerHtml + "<a href=\"browse.html" + newQuery + "\">" + page + "</a>";
                }
            }
            $element.html(pagerHtml).show();
        }
    };

    $.fn.extend({

        aeLoginForm: function(options) {
            return this.each(function() {
			new $.AELoginForm(this, options);
            });
        }
    });

    $.AELoginForm = function(form, opts) {

        var $form = $(form);
        var $user = $form.find("input[name='u']").first();
        var $pass = $form.find("input[name='p']").first();
        var $remember = $form.find("input[name='r']").first();
        var $submit = $form.find("input[name='s']").first();
        var options = opts
        var $status = $(options.status);

        function doLogin() {
            var pass = $pass.val();

            $pass.val("");
            $status.text("");
            $submit.attr("disabled", "true");
            $.get(options.verifyURL, { u: $user.val(), p: pass }, doLoginNext);
        }

        function doLoginNext(text) {
            $submit.removeAttr("disabled");
            if ( "" != text ) {
                var loginExpiration = null;
                if ( $remember.attr("checked") ) {
                    loginExpiration = 365;
                }

                $.cookie("AeLoggedUser", $user.val(), {expires: loginExpiration, path: '/'});
                $.cookie("AeLoginToken", text, {expires: loginExpiration, path: '/'});

                window.location.href = decodeURI(window.location.pathname);
            } else {
                $status.text("Incorrect user name or password. Please try again.");
                $user.focus();
            }
        }

        $(form).submit(function() {
            doLogin();
            return false;
        });

        $user.focus();
    };

    $(function() {
        // fixes EBI iframes issue in MSIE
        if ($.browser.msie) {
            $("#head").attr("allowTransparency", true);
            $("#ae_contents").css("z-index", 1);
        }
    });

})(window.jQuery);

function
aeClearField( sel )
{
    $(sel).val("").focus();
}

