// $('.copy-button').bind('click', function(event) {
//     console.log(event.target.id);
//     event.target.title = "copied";
// });

$(document).ready(function () {

    new Clipboard('.copyable');

    $('.copy-button').tooltip();

    $('.copy-button').bind('click', function (event) {
        $('#' + event.currentTarget.id).trigger('copied', ['Copied!']);
    });

    $('.copy-button').bind('copied', function (event, message) {
        $(this).attr('title', message)
            .tooltip('fixTitle')
            .tooltip('show')
            .attr('title', "Copy to Clipboard")
            .tooltip('fixTitle');
    });
});