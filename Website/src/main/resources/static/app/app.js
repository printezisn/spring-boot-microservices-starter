import 'bulma/bulma.sass';
import './sass/app.scss';

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
        const type = toastrNotification.getAttribute('notification-type');
        
        import(/* webpackChunkName: 'toastr' */ './js/toastr.js').then(({ showToastrNotification }) => {
            showToastrNotification(type, title, message);
        });
    });
};

const initFormValidate = () => {
    const forms = Array.prototype.slice.call(document.querySelectorAll('.validate-form'));
    forms.forEach(form => {
        import(/* webpackChunkName: 'validate' */ './js/validate').then(({ initValidate }) => {
            initValidate(form);
        });
    });
};

const init = () => {
    initNavbar();
    initNotifications();
    initToastrNotifications();
    initFormValidate();
};

if(document.readyState !== 'loading') {
    init();
}
else {
    document.addEventListener('DOMContentLoaded', () => {
        init();
    });
}