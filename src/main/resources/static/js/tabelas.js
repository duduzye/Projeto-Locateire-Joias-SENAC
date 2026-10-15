(() => {
  const busca = document.getElementById('filtroTabela');
  const rows = [...document.querySelectorAll('#tabelaFiltravel tbody tr')];
  if (!busca) return;
  busca.addEventListener('input', () => {
    const termo = busca.value.toLocaleLowerCase('pt-BR').trim();
    rows.forEach(linha => { linha.hidden = !linha.textContent.toLocaleLowerCase('pt-BR').includes(termo); });
  });
})();
