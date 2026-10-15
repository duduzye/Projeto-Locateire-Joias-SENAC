(() => {
  const itens = document.getElementById('itensVenda');
  const template = document.getElementById('modeloItem');
  const adicionar = document.getElementById('adicionarItem');
  const total = document.getElementById('totalEstimado');
  if (!itens || !template) return;
  const moeda = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'});
  function atualizar() {
    let subtotal = 0;
    itens.querySelectorAll('.item-row').forEach(row => {
      const select = row.querySelector('select');
      const opt = select.selectedOptions[0];
      const quantidade = row.querySelector('input');
      const preco = Number(opt?.dataset.preco ?? 0);
      const saldo = Number(opt?.dataset.estoque ?? 0);
      quantidade.max = saldo > 0 ? String(saldo) : '';
      subtotal += preco * (Number(quantidade.value) || 0);
    });
    total.textContent = moeda.format(subtotal);
  }
  function add() {
    const row = template.content.firstElementChild.cloneNode(true);
    row.querySelector('.remove-item').addEventListener('click', () => {
      if (itens.children.length > 1) row.remove();
      atualizar();
    });
    row.querySelectorAll('input,select').forEach(el => el.addEventListener('input', atualizar));
    itens.appendChild(row); atualizar();
  }
  adicionar.addEventListener('click', add);
  add();
  document.getElementById('formVenda').addEventListener('submit', evt => {
    const rows = [...itens.querySelectorAll('.item-row')];
    if(rows.some(row => {
      const selected = row.querySelector('select').selectedOptions[0];
      const qtd = Number(row.querySelector('input').value);
      return !selected?.value || !Number.isInteger(qtd) || qtd < 1 || qtd > Number(selected.dataset.estoque || 0);
    })) { evt.preventDefault(); alert('Confira o estoque disponível e as quantidades dos itens.'); }
  });
})();
