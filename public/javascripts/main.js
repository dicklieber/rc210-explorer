
$(document).ready(function(){
    $('.callsign').keyup(function(){
        $(this).val($(this).val().toUpperCase());
    });
});

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

