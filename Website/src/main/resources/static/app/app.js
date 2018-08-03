import 'bulma/bulma.sass';
import 'toastr/toastr.scss';
import './sass/app.scss';

import { initValidate } from './js/validate';

function initNavbar() {
    const burgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'));
    burgers.forEach(el => {
        el.addEventListener('click', () => {
            const targetName = el.dataset.target;
            const target = document.getElementById(targetName);

            el.classList.toggle('is-active');
            target.classList.toggle('is-active');
        });
    });
}

function initNotifications() {
    const notifications = Array.prototype.slice.call(document.querySelectorAll('.notification'));
    notifications.forEach(notification => {
        const deleteButton = notification.querySelector('.delete');
        if (deleteButton) {
            deleteButton.addEventListener('click', () => {
                notification.parentNode.removeChild(notification);
            });
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initNotifications();
    initValidate();
});