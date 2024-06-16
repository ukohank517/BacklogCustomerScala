document.addEventListener('DOMContentLoaded', function() {
    const favTable = document.getElementById('fav-list').querySelector('tbody');
    const searchResultsTable = document.getElementById('search-results').querySelector('tbody');
    const searchKeywordInput = document.getElementById('search-keyword');

    // お気に入りリストをローカルストレージから取得してチェックボックスを更新
    const favList = JSON.parse(localStorage.getItem('favList')) || [];
    favList.forEach(id => {
        const row = searchResultsTable.querySelector(`tr[data-id="${id}"]`);
        if (row) {
            row.querySelector('.fav-checkbox').checked = true;
            favTable.appendChild(row);
            sortTable(favTable);
        }
    });

    // お気に入りリストの変更をローカルストレージに保存
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('fav-checkbox')) {
            const row = e.target.closest('tr');
            if (e.target.checked) { // 検索結果からお気に入りリストに追加
                favTable.appendChild(row);
                if (!favList.includes(row.dataset.id)) {
                    favList.push(row.dataset.id);
                }
            } else { // お気に入りリストから検索結果に戻す
                searchResultsTable.appendChild(row);
                const index = favList.indexOf(row.dataset.id);
                if (index > -1) {
                    favList.splice(index, 1);
                }
            }
            localStorage.setItem('favList', JSON.stringify(favList));

            // 時間順でソート
            sortTable(favTable);
            sortTable(searchResultsTable);
        }
    });

    // 検索キーワード入力時に絞り込み
    searchKeywordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const keyword = searchKeywordInput.value.toLowerCase();
            Array.from(searchResultsTable.rows).forEach(row => {
                const projectName = row.querySelector('td:nth-child(3)').textContent.toLowerCase();
                const type = row.querySelector('td:nth-child(4)').textContent.toLowerCase();
                const summary = row.querySelector('td:nth-child(5)').textContent.toLowerCase();
                if (projectName.includes(keyword) || type.includes(keyword) || summary.includes(keyword)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        }
    });

    // テーブルを日付でソート
    function sortTable(table) {
        const rows = Array.from(table.rows);
        rows.sort((a, b) => {
            const dateA = new Date(a.querySelector('td:nth-child(7)').textContent);
            const dateB = new Date(b.querySelector('td:nth-child(7)').textContent);
            return dateB - dateA;
        });
        rows.forEach(row => table.appendChild(row));
    }

    // 初期表示時にテーブルを日付でソート
    sortTable(favTable);
    sortTable(searchResultsTable);
});
