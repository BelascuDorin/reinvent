-- The curated Field vocabulary (spec 0002): the controlled set of career areas a
-- Mentor can practise and a Mentee can browse by. Seeded here rather than modelled
-- as a Java enum, so the vocabulary is business-configurable without a code
-- redeploy. slug is the stable key a Mentor profile references; display_name is
-- what visitors see; sort_order fixes a stable, simple browse order (gaps of 10
-- leave room to slot new Fields in later without renumbering).
CREATE TABLE field
(
  slug         TEXT PRIMARY KEY,
  display_name TEXT    NOT NULL,
  sort_order   INTEGER NOT NULL
);

INSERT INTO field (slug, display_name, sort_order) VALUES
  ('software-engineering', 'Software Engineering', 10),
  ('data-science',         'Data Science',         20),
  ('design',               'Design',               30),
  ('product-management',   'Product Management',   40),
  ('marketing',            'Marketing',            50),
  ('finance',              'Finance',              60),
  ('entrepreneurship',     'Entrepreneurship',     70),
  ('medicine',             'Medicine',             80),
  ('law',                  'Law',                  90),
  ('education',            'Education',            100);
