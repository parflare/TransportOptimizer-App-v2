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

var username = null;


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
    username = document.getElementById('username').innerText.trim()
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
        formData.append("userName", username);
        fetch('/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    response.text().then(result => {
                        console.log(result);
                        console.info(result);
                        changeClasses(iconElement, 'fa-solid', 'fa-cloud-download');
                        dragText.textContent = result;
                        dragSpan.textContent = 'but you can change file';

                        document.getElementById('processFileButton').classList.remove('disabled');
                        document.getElementById('optimizeFileButton').classList.remove('disabled');

                    })
                } else {
                    response.text().then(errorMessage => {
                        document.getElementById('processFileButton').classList.add('disabled');
                        document.getElementById('optimizeFileButton').classList.remove('disabled');

                        throw new Error(errorMessage);
                    })
                        .catch(error => {
                            console.log(error);
                            dropArea.classList.remove("active");
                            dropArea.classList.add("failed");
                            dragText.textContent = error;
                            dragSpan.textContent = 'try to load new file';
                            changeClasses(iconElement, 'fa-solid', 'fa-cloud');
                        });
                }
            })

    }


});

async function optimizeFile() {
    const loader = document.getElementById('optimizeLoader');
    loader.style.display = 'inline-block';
    try {
        const response = await fetch(`/optimize?userName=${username}`, {
            method: 'POST'
        });

        if (response.ok) {
            const result = await response.text();
            console.log(result);
            console.info(result);
        } else {
            console.error('Помилка при оптимізації файлу');
        }
    } catch (error) {
        console.error('Помилка при оптимізації файлу', error);
    } finally {
        loader.style.display = 'none';
    }
}

async function processFile() {
    const loader = document.getElementById('processLoader');
    loader.style.display = 'inline-block';
    try {
        const response = await fetch(`/process?userName=${username}`, {
            method: 'POST'
        });

        if (response.ok) {
            const result = await response.text();
            console.log(result);
            console.info(result);
            // Показуємо кнопку "Download zip"
            document.getElementById('downloadZipButton').classList.remove('disabled');
        } else {
            document.getElementById('downloadZipButton').classList.add('disabled');
            console.error('Помилка при обробці файлу');
        }
    } catch (error) {
        console.error('Помилка при обробці файлу', error);
    } finally {
        loader.style.display = 'none';
    }
}

function downloadFile() {
    const loader = document.getElementById('downloadLoader');
    loader.style.display = 'inline-block';
    var xhr = new XMLHttpRequest();
    xhr.open('GET', `/download?userName=${username}`, true);
    xhr.responseType = 'blob';

    xhr.onload = function (e) {
        loader.style.display = 'none';
        if (this.status === 200) {
            console.log('Response received successfully.');
            var blob = new Blob([this.response], {type: 'application/zip'});
            var url = window.URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = 'reports.zip';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        } else {
            console.error('Failed to download file:', this.statusText);
        }
    };

    xhr.onerror = function () {
        loader.style.display = 'none';
        console.error('Network error occurred.');
    };

    xhr.send();
}
