function slowScroll(id) {
    $('html, body').animate({
        scrollTop: $(id).offset().top - 100
    }, 500);
}

$(document).on("scroll", function () {
    if ($(window).scrollTop() === 0)
        $("header").removeClass("fixed");
    else
        $("header").attr("class", "fixed");
});

$(document).ready(function () {

    document.querySelectorAll('.menu a').forEach(link => {
        link.addEventListener('dragstart', (event) => {
            event.preventDefault();
        });
    });

//selecting all required elements
    const dropArea = document.querySelector(".drag-area"),
        iconElement = document.querySelector('.icon i');
    let dragText = dropArea.querySelector("p"),
        dragSpan = dropArea.querySelector("span"),
        button = dropArea.querySelector("button"),
        input = dropArea.querySelector("input");
    var file; //this is a global variable and we'll use it inside multiple functions


    button.onclick = () => {
        input.click(); //if user click on the button then the input also clicked
    }


    input.addEventListener("change", function () {
        //getting user select file and [0] this means if user select multiple files then we'll select only the first one
        dropArea.classList.remove("failed");
        dropArea.classList.add("active");
    });
//If user Drag File Over DropArea
    dropArea.addEventListener("dragover", (event) => {
        event.preventDefault(); //preventing from default behaviour
        changeClasses(iconElement, 'fa-solid', 'fa-cloud-upload');
        dropArea.classList.remove("failed");
        dropArea.classList.add("active");
        dragText.textContent = "Release to Upload File";
    });

//If user leave dragged File from DropArea
    dropArea.addEventListener("dragleave", () => {
        dropArea.classList.remove("active");
        dragText.textContent = "Drag & Drop to Upload File";
    });

//If user drop File on DropArea
    dropArea.addEventListener("drop", (event) => {
        event.preventDefault(); //preventing from default behaviour
        //getting user select file and [0] this means if user select multiple files then we'll select only the first one
        dropArea.classList.remove("failed");
        dropArea.classList.add("active");

        file = event.dataTransfer.files[0];
        loadFile();
    });

    document.getElementById('fileInput').addEventListener('change', ev => {
        file = document.getElementById('fileInput').files[0];
        loadFile();
    });

    function changeClasses(element, defaultClass, newClass) {
        for (let i = 0; i < element.classList.length; i++) {
            let currentClass = element.classList.item(i);
            if (currentClass !== defaultClass) {
                element.classList.remove(currentClass);
            }
        }
        element.classList.add(newClass);
    }

    function loadFile() {
        var formData = new FormData();
        formData.append("file", file);

        fetch('/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    response.text().then(result => {
                        console.log(result);
                        changeClasses(iconElement, 'fa-solid', 'fa-cloud-download');
                        dragText.textContent = result;
                        dragSpan.textContent = 'but you can change file';
                    })
                } else {
                    response.text().then(errorMessage => {
                        throw new Error(errorMessage);
                    })
                        .catch(error => {
                            console.log(error);
                            dropArea.classList.remove("active");
                            dropArea.classList.add("failed");
                            dragText.textContent = error;
                            dragSpan.textContent = 'but you can change file';
                            changeClasses(iconElement, 'fa-solid', 'fa-cloud');


                        });
                }
            })

    }
});