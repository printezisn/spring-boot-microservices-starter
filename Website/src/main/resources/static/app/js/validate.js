import FormValidator from 'validate-js';

let cleanErrors = element => {
    element.classList.remove('is-danger');
    
    const messageElement = document.getElementById(element.id + '-message');
    messageElement.style.display = 'none';
};

let showError = (element, rule) => {
    element.classList.add('is-danger');
    
    const messageElement = document.getElementById(element.id + '-message');
    switch(rule) {
        case 'required':
            messageElement.innerHTML = element.getAttribute('required-field');
            break;
        case 'matches':
            messageElement.innerHTML = element.getAttribute('matches-field-error');
            break;
    }    
    
    messageElement.style.display = '';
};

export let initValidate = () => {
    const forms = Array.prototype.slice.call(document.querySelectorAll('.validate-form'));
    forms.forEach(form => {
        let fields = [];
        const elements = Array.prototype.slice.call(form.querySelectorAll('.input'));
        
        elements.forEach(element => {
            let rules = [];
            
            if(element.getAttribute('required-field')) {
                rules.push('required');
            }
            if(element.getAttribute('matches-field')) {
                rules.push('matches[' + element.getAttribute('matches-field') + ']');
            }
            
            fields.push({ name: element.name, rules: rules.join('|') });
        });
        
        new FormValidator(form.name, fields, errors => {
            elements.forEach(element => {
                cleanErrors(element);
                errors.forEach(error => {
                    if(error.name === element.name) {
                        showError(element, error.rule);
                    }
                });
            });
        });
    });
};