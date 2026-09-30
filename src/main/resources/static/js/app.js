$(function () {
  // Menu mobile (sidebar en tiroir sous 992px)
  $('#menuToggle').on('click', function () {
    $('body').toggleClass('sidebar-open');
  });
  $('#sidebarOverlay').on('click', function () {
    $('body').removeClass('sidebar-open');
  });

  // Modale de confirmation partagee : appliquer la classe "js-confirm-action" a un
  // bouton de soumission (confirme le formulaire le plus proche) ou a un lien <a>
  // (suit son href apres confirmation), avec un message optionnel via
  // data-confirm-message.
  var $confirmTarget = null;
  $(document).on('click', '.js-confirm-action', function (e) {
    e.preventDefault();
    $confirmTarget = $(this);
    var message = $(this).attr('data-confirm-message') || 'Etes-vous sur de vouloir continuer ?';
    $('#confirmModalMessage').text(message);
    $('#confirmModal').modal('show');
  });
  $('#confirmModalBtn').on('click', function () {
    $('#confirmModal').modal('hide');
    if (!$confirmTarget) {
      return;
    }
    if ($confirmTarget.is('a')) {
      window.location.href = $confirmTarget.attr('href');
    } else {
      $confirmTarget.closest('form').trigger('submit');
    }
    $confirmTarget = null;
  });
});
