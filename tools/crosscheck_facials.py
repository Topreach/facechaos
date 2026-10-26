"""Export reproducible synthetic facial-channel cross-check results."""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'recognition'))
sys.path.insert(0, str(ROOT / 'tests'))
from facial_scenarios import crosscheck_matrix
from lab_core import BY_KEY


def main():
    matrix = crosscheck_matrix()
    results = {'kind': 'synthetic channel isolation; not human-camera recognition validation',
               'sections': len(matrix), 'passed': sum(row['passed'] for row in matrix.values()),
               'limits': 'Visual scores and personal-cheek scores are fixtures in this matrix. '
                         'Separate synthetic pixel/appearance unit tests exercise those algorithms. '
                         'No real participant, precision/recall, false-trigger or latency result is claimed.',
               'rows': {key: {'label': BY_KEY[key].title, **row} for key, row in matrix.items()}}
    target = ROOT / 'docs/facial-crosscheck-results.json'
    target.write_text(json.dumps(results, indent=2), encoding='utf-8')
    print(f'Synthetic facial channel cross-check: {results["passed"]}/{results["sections"]} passed')
    if results['passed'] != results['sections']:
        for key, row in matrix.items():
            if not row['passed']:
                print(key, row)
        raise SystemExit(1)


if __name__ == '__main__':
    main()
