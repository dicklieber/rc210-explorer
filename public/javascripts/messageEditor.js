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
    $("#dropZone").droppable({
        accept: ".wordSource",
        drop: function (event, ui) {
            const sourceTr = ui.draggable;
            $("#currentList").append(sourceTr);

        },
        over: function (event, ui) {
            let targetId = event.target.id;
            console.log(`over::targetId: ${targetId}`)

        }
    });

    $("#currentList").sortable();

    $('#messageForm').on('submit', function () {
        const $currentList = $("#currentList");
        const words = $currentList.children()
            .map(function () {
                return this.dataset.wordid;
            })
            .get();

        $("#words").val(words)

        const formData = new FormData($('form')[0]);
        formData.append("words", words);
        return true;
    });
});


/**
 * Remove the word in #currentList
 * Does nothing if anyplace else. e.g. The #availableList.
 * @param liElement in
 */
function remove(liElement) {
    const inCurrentlist = $(liElement).closest("#currentList");
    const s = inCurrentlist.size();
    if (s > 0) {
        liElement.remove();
    }
}

function filterFunction() {
    const filter = $("#filter").val().toUpperCase();

    $("#availableList li").each(function () {
        const $1 = $(this);
        const wordText = $1.text();
        const value = wordText.toUpperCase();
        if(value.includes(filter) )
            $1.show();
        else
            $1.hide();
    });

    // const div = document.getElementById("myDropdown");
    // const a = div.getElementsByTagName("tr");
    // for (i = 0; i < a.length; i++) {
    //     txtValue = a[i].textContent || a[i].innerText;
    //     if (txtValue.toUpperCase().indexOf(filter) > -1) {
    //         a[i].style.display = "";
    //     } else {
    //         a[i].style.display = "none";
    //     }
    // }
}

