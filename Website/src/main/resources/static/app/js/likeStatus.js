const load = (movieId, totalLikes, hideOnLoad, showOnLoad, hideOnLike, showOnLike) => {
    initLoading(hideOnLoad, showOnLoad);

    fetch('/movie/likestatus/' + movieId, {
        method : 'GET',
        headers : {
            'Accept' : 'application/json'
        },
        credentials : 'same-origin'
    })
    .then(response => response.json())
    .then(response => {
        showOnLoad.forEach(el => el.style.display = 'none');
        hideOnLoad.forEach(el => el.style.display = '');

        showOnLike.forEach(el => el.style.display = (response.hasLiked ? '' : 'none'));
        hideOnLike.forEach(el => el.style.display = (response.hasLiked ? 'none' : ''));

        totalLikes.innerHTML = response.totalLikes;
    })
};

const initLoading = (hideOnLoad, showOnLoad) => {
    showOnLoad.forEach(el => el.style.display = '');
    hideOnLoad.forEach(el => el.style.display = 'none');
};

export const createLikeStatus = element => {
    const movieId = element.getAttribute('movie-id');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrf = document.querySelector('meta[name="_csrf"]').content;
    const totalLikes = element.querySelector('.total-likes');
    const hideOnLoad = Array.prototype.slice.call(element.querySelectorAll('.hide-on-load'));
    const showOnLoad = Array.prototype.slice.call(element.querySelectorAll('.show-on-load'));
    const hideOnLike = Array.prototype.slice.call(element.querySelectorAll('.hide-on-like'));
    const showOnLike = Array.prototype.slice.call(element.querySelectorAll('.show-on-like'));
    const likeButtons = Array.prototype.slice.call(element.querySelectorAll('.like-button'));
    const unlikeButtons = Array.prototype.slice.call(element.querySelectorAll('.unlike-button'));

    const headers = {
        'Content-Type' : 'application/x-www-form-urlencoded',
        'Accept' : 'application/json'
    };
    headers[csrfHeader] = csrf;

    likeButtons.forEach(button => {
        button.addEventListener('click', () => {
            initLoading(hideOnLoad, showOnLoad);

            fetch('/movie/like', {
                method : 'post',
                headers : headers,
                credentials : 'same-origin',
                body : `id=${movieId}`,
            })
            .then(() => load(movieId, totalLikes, hideOnLoad, showOnLoad, hideOnLike, showOnLike));
        });
    });

    unlikeButtons.forEach(button => {
        button.addEventListener('click', () => {
            initLoading(hideOnLoad, showOnLoad);

            fetch('/movie/unlike', {
                method : 'post',
                headers : headers,
                credentials : 'same-origin',
                body : `id=${movieId}`,
            })
            .then(() => load(movieId, totalLikes, hideOnLoad, showOnLoad, hideOnLike, showOnLike));
        });
    });

    load(movieId, totalLikes, hideOnLoad, showOnLoad, hideOnLike, showOnLike);
};