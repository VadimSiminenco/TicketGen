const group = document.getElementById('group');
const student = document.getElementById('student');
const generate = document.getElementById('generate');
const statusArea = document.getElementById('status');
const resultDialog = document.getElementById('result-dialog');
const lockDialog = document.getElementById('lock-dialog');
let pendingSelection = null;
let busy = false;

function updateButton() {
    generate.disabled = busy || !group.value || !student.value || group.disabled;
}
function resetStudents() {
    student.replaceChildren(new Option('Выберите студента', ''));
    student.disabled = true;
    updateButton();
}
group.addEventListener('change', async () => {
    resetStudents();
    statusArea.textContent = '';
    const selectedGroup = group.value;
    if (!selectedGroup) return;
    try {
        const response = await fetch('/api/students?group=' + encodeURIComponent(selectedGroup));
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Не удалось загрузить студентов.');
        if (group.value !== selectedGroup) return;
        data.forEach(item => student.add(new Option(item.label, item.key)));
        student.disabled = data.length === 0;
        if (data.length === 0) statusArea.textContent = 'В выбранной группе нет студентов.';
    } catch (error) {
        statusArea.textContent = error.message;
    }
    updateButton();
});
student.addEventListener('change', updateButton);

async function submitSelection(selection) {
    busy = true;
    updateButton();
    statusArea.textContent = '';
    try {
        const response = await fetch('/api/generate', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(selection)
        });
        const data = await response.json();
        if (!response.ok) {
            if (data.code === 'locked') {
                pendingSelection = selection;
                if (!lockDialog.open) lockDialog.showModal();
            } else {
                statusArea.textContent = data.message || 'Не удалось выдать билет.';
            }
            return;
        }
        pendingSelection = null;
        lockDialog.close();
        document.getElementById('ticket-number').textContent = data.ticketNumber;
        const questions = document.getElementById('questions');
        questions.replaceChildren();
        data.questions.forEach(question => {
            const item = document.createElement('li');
            item.textContent = question;
            questions.append(item);
        });
        resultDialog.showModal();
    } catch (error) {
        statusArea.textContent = 'Не удалось связаться с приложением. Попробуйте ещё раз.';
    } finally {
        busy = false;
        updateButton();
    }
}
document.getElementById('ticket-form').addEventListener('submit', event => {
    event.preventDefault();
    if (!generate.disabled) submitSelection({group: group.value, studentKey: student.value});
});
document.getElementById('retry').addEventListener('click', () => {
    if (pendingSelection && !busy) submitSelection(pendingSelection);
});
document.getElementById('return').addEventListener('click', () => {
    pendingSelection = null;
    lockDialog.close();
});
document.getElementById('close-result').addEventListener('click', () => resultDialog.close());
