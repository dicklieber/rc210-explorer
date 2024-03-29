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
    $(function () {
        $("#currentList").sortable();
    });

    $('#form').on('submit', function () {
        const $currentList = $("#currentList");
        const wordIds = $currentList.children()
            .map(function () {
                return this.dataset.rcnumber;
            }).get();
        const csv = wordIds.join();
        $("#ids").val(csv);

        return true;
    });


});

function filterFunction() {
    const filter = $("#filter").val().toUpperCase();

    $("#availableList li").each(function () {
        const $1 = $(this);
        const wordText = $1.text();
        const value = wordText.toUpperCase();
        if (value.includes(filter))
            $1.show();
        else
            $1.hide();
    });
}

/**
 * Remove the word in #currentList
 * Does nothing if anyplace else. e.g. The #availableList.
 * @param liElement in
 */
function dblClick(liElement) {
    const classes = liElement.classList;
    const b = classes.contains("selected");
    if (b) {
        remove(liElement)
    } else {
        moveToCurrent(liElement)
    }
}

function remove(liElement) {
    const data = (liElement.childNodes)[0].data;
    $.confirm({
        title: 'Confirm',
        content: 'Remove ' + data,
        buttons: {
            confirm: function () {
                liElement.remove();
            },
            cancel: function () {

            }
        }
    });
}

function moveToCurrent(from) {
    const li = from;
    const currentList = $("#currentList")
    const rcNumber = li.dataset.rcnumber;

    const str = li.innerHTML;
    var newLi = document.createElement('li');
    newLi.setAttribute("data-rcnumber", rcNumber);
    newLi.innerHTML = str
    currentList.append(newLi);


}
