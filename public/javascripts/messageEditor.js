/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

$(function () {
    $(".wordSource").draggable({
        appendTo: 'body',
        containment: 'window',
        scroll: false,
        helper: "clone"
    });
    $(".currentWords").draggable({
        // containment: "parent",
        helper: "clone"
    });
    $(".wordDrop").droppable({

        drop: function (event, ui) {
            let targetId = event.target.id;
            let sourceTr = ui.draggable;
            console.log(`dropped ${sourceTr} onto ${targetId}`)

            if (targetId === "trash") {
                $("#ddMessage").text(`Remove this function: ${targetId}`)
            } else {
                const newId = self.crypto.randomUUID();
                sourceTr.attr("id", newId)

                insert(sourceTr, targetId)
            }
            $(this)
                .addClass("ui-state-highlight")
                .find("p")
                .html("Dropped!");


        },
        over: function (event, ui) {
            let targetId = event.target.id;
            console.log(`over::targetId: ${targetId}`)

            if (targetId === "trash") {
                // remove from table
            } else {
                // insert
            }

        }
    });
    $(".dropTrash").droppable({

        drop: function (event, ui) {
            let targetId = event.target.id;
            let sourceTr = ui.draggable;
            console.log(`dropped in trash ${sourceTr} onto ${targetId}`)

            const draggable = ui.draggable;
            draggable.remove()
        },
        over: function (event, ui) {
            const target = event.target;
            const targetId = target.id;
            console.log(`over::targetId: ${targetId}`)

            if (targetId === "trash") {
                // remove from table
            } else {
                // insert
            }

        }
    });

    $('#messageForm').on('submit', function () {

        var words = $("#currentWords tr")
            .map(function () {
                console.log(`this: ${this}`);
                const dataset =  this.dataset;
                console.log(`dataset: ${dataset}`);
                const f =  dataset.word
                return f;
            }) //Project Ids
            .get(); //ToArray

        let tf = typeof words

        console.log(words)

        $("#words").val(words)

        let formData = new FormData($('form')[0]);
        formData.append("words", words);
        // $("#btnSubmit").prop("disabled", true);

        return true;
    });
});

// Insert into currnbt
function insert(dropee, targetId) {
    let elementToDropBefore = $("#" + targetId);
    console.log("addFunction::  elementToDropBefore: " + elementToDropBefore);
    elementToDropBefore.before(dropee);
}

function potentialTrashDrop() {
    console.log("potentialTrashDrop");
}