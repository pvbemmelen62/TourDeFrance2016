/**
 * Code from file site.min.js from www.sudokuspoiler.net
 * beautified by http://jsbeautifier.org/
 */
var sudoku = function() {
    var e, b = [],
        c = [],
        h = [],
        f = [],
        d = 0,
        g = window.location.pathname.substring(window.location.pathname.lastIndexOf("/") + 1);
    return e = {
        readFromStorage: function() {
            $(".swappable").each(function(a) {
                h[a] = $(this).data("group")
            });
            b = JSON.parse(window.localStorage.getItem(g + "-board")) || [];
            c = JSON.parse(window.localStorage.getItem(g + "-group")) || h.slice(0);
            e.initBoard()
        },
        writeToStorage: function() {
            e.readBoard();
            window.localStorage.setItem(g + "-board", JSON.stringify(b));
            0 < c.length && window.localStorage.setItem(g +
                "-group", JSON.stringify(c))
        },
        solve: function() {
            $("#solveButton").attr("disabled", "disabled");
            $("#resetButton").attr("disabled", "disabled");
            $("#status").removeClass("error").text("Solving...");
            e.readBoard();
            $.ajax({
                url: window.location.href,
                type: "POST",
                datatype: "json",
                data: JSON.stringify({
                    board: b,
                    group: c
                }),
                processData: !1,
                contentType: "application/json; charset=utf-8",
                success: function(a) {
                    $("#solveButton").removeAttr("disabled");
                    $("#resetButton").removeAttr("disabled");
                    "string" === typeof a ? $("#status").addClass("error").text(a) :
                        0 === a.length ? $("#status").text("No Solutions") : ($("#solveButton").hide(), $("#unsolveButton").show(), $("input:text").attr("readonly", "readonly").filter(function() {
                            return this.value.match(/^\s*$/)
                        }).addClass("empty"), $(".swappable").draggable("option", "disabled", !0), f = a, d = 0, e.writeBoard())
                },
                error: function() {
                    $("#solveButton").removeAttr("disabled");
                    $("#resetButton").removeAttr("disabled");
                    $("#status").addClass("error").text("Network communication error. Please try again later")
                }
            })
        },
        readBoard: function() {
            $("input:text").each(function(a) {
                $(this).hasClass("empty") ?
                    b[a] = "" : b[a] = $.trim($(this).val())
            });
            $(".swappable").each(function(a) {
                c[a] = $(this).data("group")
            })
        },
        writeBoard: function() {
            $("input:text").each(function(a) {
                $(this).val(f[d][a])
            });
            $("#status").text("Solution " + (d + 1) + " of " + f.length);
            0 === d ? $("#previousLink").css("visibility", "hidden") : $("#previousLink").css("visibility", "visible");
            d === f.length - 1 ? $("#nextLink").css("visibility", "hidden") : $("#nextLink").css("visibility", "visible")
        },
        initBoard: function() {
            $("#status").removeClass("error").text("Enter numbers");
            $("#previousLink").css("visibility", "hidden");
            $("#nextLink").css("visibility", "hidden");
            $("#unsolveButton").hide();
            $("#solveButton").show();
            $("input:text").removeAttr("readonly").removeClass("empty");
            $(".swappable").draggable("option", "disabled", !1);
            $("input:text").each(function(a) {
                $(this).val(b[a])
            });
            $(".swappable").each(function(a) {
                $(this).data("group", c[a])
            });
            $(".swappable").attr("class", function(a, b) {
                return b.replace(/color[0-9]+/, "color" + c[a])
            })
        },
        resetBoard: function() {
            b = [];
            c = h.slice(0);
            e.initBoard()
        },
        movePrevious: function() {
            d--;
            e.writeBoard();
            return !1
        },
        moveNext: function() {
            d++;
            e.writeBoard();
            return !1
        }
    }
}();
$(function() {
    $("#solveButton").click(sudoku.solve);
    $("#unsolveButton").click(sudoku.initBoard);
    $("#resetButton").click(sudoku.resetBoard);
    $("#nextLink").click(sudoku.moveNext);
    $("#previousLink").click(sudoku.movePrevious);
    $(".swappable").draggable({
        helper: "clone",
        cancel: null,
        snap: !0
    });
    $(".swappable").droppable({
        accept: ".swappable",
        tolerance: "intersect",
        drop: function(e, b) {
            var c = $(this).attr("class").match(/color[0-9]+/),
                h = $(this).data("group"),
                f = $(this).val(),
                d = b.draggable.attr("class").match(/color[0-9]+/),
                g = b.draggable.data("group"),
                a = b.draggable.val();
            $(this).attr("class", function(a, b) {
                return b.replace(c, d)
            });
            $(this).data("group", g);
            $(this).val(a);
            b.draggable.attr("class", function(a, b) {
                return b.replace(d, c)
            });
            b.draggable.data("group", h);
            b.draggable.val(f)
        }
    });
    sudoku.readFromStorage()
});
window.onbeforeunload = function() {
    sudoku.writeToStorage()
};