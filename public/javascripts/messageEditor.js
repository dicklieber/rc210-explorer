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


    $(".wordSource").on("dblclick", function () {
        const li = this;
        const $currentList = $("#currentList");
        const size = $currentList.children().size();
        if (size >= 9) {
            $("#maxWordsAlert").show()
        } else {
            $currentList.append($(li).clone());
        }
    });

    $("#currentList").sortable({
        sort: function () {
            // gets added unintentionally by droppable interacting with sortable
            // using connectWithSortable fixes this, but doesn't allow you to customize active/hoverClass options
            $(this).removeClass("active");
        }
    });

    $('#messageForm').on('submit', function () {
        const $currentList = $("#currentList");
        const wordIds = $currentList.children()
            .map(function () {
                return this.dataset.wordid;
            }).get();
        const csv = wordIds.join();
        $("#words").val(csv);

        const formData = new FormData($('form')[0]);
        formData.append("words", wordIds);
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
        if (value.includes(filter))
            $1.show();
        else
            $1.hide();
    });

}

