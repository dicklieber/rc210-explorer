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
    // $(".currentWords").draggable({
    //     // containment: "parent",
    //     helper: "clone"
    // });
    $("#dropZone").droppable({
        drop: function (event, ui) {
            let targetId = event.target.id;
            let sourceTr = ui.draggable;
            console.log(`dropped ${sourceTr} onto ${targetId}`)

            $("#currentList").append(sourceTr);
            // insert(sourceTr, targetId)

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

    $("#currentList").sortable();

    $('#messageForm').on('submit', function () {
        const $currentList = $("#currentList");
        var words = $currentList.children()
            .map(function () {
                console.log(`this: ${this}`);
                const dataset = this.dataset;
                console.log(`dataset: ${dataset}`);
                const f = dataset.word
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


function remove() {
    console.log(`remove: dropee: ${dropee} targetId: ${targetId}`)
    $(this).parent().detach();
}

// Insert into currnbt
// function insert(dropee, targetId) {
//     let elementToDropBefore = $("#" + targetId);
//     console.log("addFunction::  elementToDropBefore: " + elementToDropBefore);
//     elementToDropBefore.before(dropee);
// }

