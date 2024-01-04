CREATE OR REPLACE FUNCTION updated()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_on = now();
  RETURN NEW;
END;
$$ language 'plpgsql';
