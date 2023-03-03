function handleClick(cb, param) {
    console.log("Clicked, new value = " + cb.checked + "param: " + param);
    var hiddenCb = document.getElementById(param)
    hiddenCb.setAttribute('value',cb.checked())
}