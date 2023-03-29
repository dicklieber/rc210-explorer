function handleClick(cb, param) {
    console.log("Clicked, new value = " + cb.checked + "param: " + param);
    var hiddenCb = document.getElementById(param)
    hiddenCb.setAttribute('value',cb.checked)
}

// function handleClick(cb, param) {
//     console.log("Clicked, new value = " + cb.checked + "    param: " + param);
//     const newState = cb.checked;
//
//     const selector = "#" + param;
//     console.log("selector: " + selector)
//
//     $( selector ).each(function( index ) {
//         console.log( index + ": " + $( this ).text() );
//     });
//
//
//     $(selector).val(newState)
//     const updated = $(selector).val();
//     console.log(`updated: ${updated}`)
// }

// function myFunction() {
//     document.getElementById("myDropdown").classList.toggle("show");
// }
function filterFunction() {
    var input, filter, ul, li, a, i;
    input = document.getElementById("myInput");
    filter = input.value.toUpperCase();
    div = document.getElementById("myDropdown");
    a = div.getElementsByTagName("tr");
    for (i = 0; i < a.length; i++) {
        txtValue = a[i].textContent || a[i].innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            a[i].style.display = "";
        } else {
            a[i].style.display = "none";
        }
    }
}

