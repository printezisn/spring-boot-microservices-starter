import 'bulma/bulma.sass';
import 'toastr/toastr.scss';
import './sass/app.scss';

import toastr from 'toastr';

import { initValidate } from './js/validate';

const initNavbar = () => {
    const burgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'));
    burgers.forEach(el => {
        el.addEventListener('click', () => {
            const targetName = el.dataset.target;
            const target = document.getElementById(targetName);

            el.classList.toggle('is-active');
            target.classList.toggle('is-active');
        });
    });
};

const initNotifications = () => {
    const notifications = Array.prototype.slice.call(document.querySelectorAll('.notification'));
    notifications.forEach(notification => {
        const deleteButton = notification.querySelector('.delete');
        if (deleteButton) {
            deleteButton.addEventListener('click', () => {
                notification.parentNode.removeChild(notification);
            });
        }
    });
};

const initToastrNotifications = () => {
    const toastrNotifications = Array.prototype.slice.call(document.querySelectorAll('.toastr-notification'));
    toastrNotifications.forEach(toastrNotification => {
        const title = toastrNotification.getAttribute("notification-title");
        const message = toastrNotification.innerHTML;

        switch (toastrNotification.getAttribute('notification-type')) {
            case "success":
                toastr.success(message, title);
                break;
            case "warning":
                toastr.warning(message, title);
                break;
            case "error":
                toastr.error(message, title);
                break;
            default:
                toastr.info(message, title);
                break;
        }
    });
};

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initNotifications();
    initToastrNotifications();
    initValidate();
});