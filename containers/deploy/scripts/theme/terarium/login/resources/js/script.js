// Adds show/hide icon to the passowrd input
setTimeout(() => {
  var fToggle = function ($password, e) {
    e.preventDefault();
    const type = $password.getAttribute('type') === 'password' ? 'text' : 'password';
    $password.setAttribute('type', type);
    this.classList.toggle('pi-eye');
  };

  var createReveal = function (passwordId, toggleId) {
    var password = document.getElementById(passwordId);
    if ((password) && (password.style) && (password.style.display !== 'none')) {
      var icon = document.createElement("i");
      icon.id = toggleId;
      icon.classList.add('password-reveal', 'pi', 'pi-eye-slash');
      icon.addEventListener('click', fToggle.bind(icon, password));
      password.parentNode.insertBefore(icon, password.nextSibling);
    }
  };

  createReveal('password', 'togglePassword');
  createReveal('password-new', 'togglePasswordNew');
  createReveal('password-confirm', 'togglePasswordConfirm');
}, 5);
