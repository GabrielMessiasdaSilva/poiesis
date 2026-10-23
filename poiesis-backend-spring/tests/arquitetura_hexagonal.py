"""Verifica a modularização e a separação de responsabilidades dos serviços Poiesis.

Execute: python3 poiesis-backend-spring/tests/arquitetura_hexagonal.py
"""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SERVICES = ("produto", "login", "pedido", "producao", "relatorio")


def sources():
    for service in SERVICES:
        for module in ("domain", "spring"):
            yield from (ROOT / service / module / "src").glob("*/java/**/*.java")


class ArchitectureTests(unittest.TestCase):
    def test_maven_modules_are_domain_and_spring(self):
        ns = {"m": "http://maven.apache.org/POM/4.0.0"}
        for service in SERVICES:
            with self.subTest(service=service):
                pom = ET.parse(ROOT / service / "pom.xml").getroot()
                self.assertEqual([m.text for m in pom.findall("m:modules/m:module", ns)], ["domain", "spring"])
                self.assertFalse((ROOT / service / "springframework").exists())

    def test_packages_correspond_to_directories(self):
        for path in sources():
            with self.subTest(file=path.relative_to(ROOT)):
                source_root = next(parent for parent in path.parents if parent.name == "java")
                package = re.search(r"^package ([\w.]+);", path.read_text(), re.M).group(1)
                self.assertEqual(path.parent.relative_to(source_root).as_posix(), package.replace(".", "/"))

    def test_no_duplicate_types_between_modules_or_framework_suffix(self):
        seen = set()
        for path in sources():
            if "/src/main/" not in str(path):
                continue
            package = re.search(r"^package ([\w.]+);", path.read_text(), re.M).group(1)
            name = package + "." + path.stem
            with self.subTest(file=path.relative_to(ROOT)):
                self.assertNotIn(name, seen)
                self.assertNotRegex(package, r"^com\.poiesis\.\w+\.spring(?:\.|$)")
                seen.add(name)

    def test_internal_imports_reference_existing_sources(self):
        files = list(sources())
        types = {re.search(r"^package ([\w.]+);", path.read_text(), re.M).group(1) + "." + path.stem
                 for path in files}
        for path in files:
            for dependency in re.findall(r"^import (?:static )?(com\.poiesis\.[\w.*]+);", path.read_text(), re.M):
                with self.subTest(file=path.relative_to(ROOT), dependency=dependency):
                    if dependency.endswith(".*"):
                        self.assertTrue(any(name.startswith(dependency[:-1]) for name in types))
                    else:
                        self.assertTrue(any(dependency == name or dependency.startswith(name + ".") for name in types))

    def test_domain_depends_only_on_jdk_and_its_own_service(self):
        domain_types = set()
        for service in SERVICES:
            for path in (ROOT / service / "domain/src/main/java").rglob("*.java"):
                package = re.search(r"^package ([\w.]+);", path.read_text(), re.M).group(1)
                domain_types.add(package + "." + path.stem)
        for path in sources():
            if "/domain/src/main/" not in str(path):
                continue
            service = path.relative_to(ROOT).parts[0]
            for dependency in re.findall(r"^import (?:static )?([\w.*]+);", path.read_text(), re.M):
                with self.subTest(file=path.relative_to(ROOT), dependency=dependency):
                    self.assertTrue(dependency.startswith(("java.", f"com.poiesis.{service}.")))
                    if dependency.startswith("com.poiesis."):
                        if dependency.endswith(".*"):
                            self.assertTrue(any(name.startswith(dependency[:-1]) for name in domain_types))
                        else:
                            self.assertTrue(any(dependency == name or dependency.startswith(name + ".")
                                                for name in domain_types))
            self.assertNotRegex(path.read_text(), r"(?:org\.springframework|jakarta\.)")

    def test_controllers_do_not_access_database_or_remote_implementations(self):
        for path in sources():
            if "/src/main/" in str(path) and "/spring/src/main/" in str(path) and "/controller/" in str(path):
                with self.subTest(file=path.relative_to(ROOT)):
                    self.assertNotRegex(path.read_text(), r"com\.poiesis\.\w+\.(?:repository|integration)\.|jakarta\.persistence|org\.springframework\.data\.")

    def test_repositories_do_not_depend_on_controllers_or_http(self):
        for path in sources():
            if "/src/main/" in str(path) and "/spring/src/main/" in str(path) and "/repository/" in str(path):
                with self.subTest(file=path.relative_to(ROOT)):
                    self.assertNotRegex(path.read_text(), r"com\.poiesis\.\w+\.(?:controller|integration)\.|org\.springframework\.web\.")

    def test_orm_and_jpa_repositories_have_distinct_locations(self):
        for path in sources():
            if "/src/main/" not in str(path):
                continue
            text = path.read_text()
            with self.subTest(file=path.relative_to(ROOT)):
                if re.search(r"@(Entity|Embeddable)\b", text):
                    self.assertIn("/repository/orm/", str(path))
                    self.assertTrue(path.stem.endswith("OrmJpa"))
                if "extends JpaRepository<" in text:
                    self.assertIn("/repository/jpa/", str(path))
                    self.assertTrue(path.stem.endswith("RepositoryWithJpa"))
                if "/controller/adapter/" in str(path):
                    self.assertNotRegex(text, r"\b\w+OrmJpa\b|jakarta\.persistence")

    def test_names_follow_project_conventions(self):
        for path in sources():
            with self.subTest(file=path.relative_to(ROOT)):
                self.assertNotIn("/adapter/in/", str(path))
                self.assertNotIn("/adapter/out/", str(path))
                self.assertNotIn("/port/", str(path))
                self.assertNotIn("/usecase/", str(path))
                self.assertNotRegex(path.stem, r"UseCase|RepositoryPort|PersistenceAdapter")
                if "/domain/" in str(path):
                    self.assertNotIn("/model/", str(path))
                if re.search(r"\bclass \w+RepositoryImpl\b", path.read_text()):
                    self.assertIn("/repository/", str(path))
                    self.assertRegex(path.read_text(), r"implements \w+Repository\b")


if __name__ == "__main__":
    unittest.main(verbosity=2)
