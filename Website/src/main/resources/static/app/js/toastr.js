import 'toastr/toastr.scss';
import '../sass/toastr-overrides.scss';

import toastr from 'toastr';

export const showToastrNotification = (type, title, message) => {
    switch (type) {
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
};