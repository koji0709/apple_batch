CREATE TABLE "local_history" (
  "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "clz_name" text,
  "stage" text,
  "row_json" text,
  "create_time" integer
);
CREATE TABLE "purchase_record" (
  "apple_id" text NOT NULL,
  "purchase_id" text NOT NULL,
  "weborder" text NOT NULL,
  "purchase_date" integer NOT NULL,
  "estimated_total_amount" text,
  "plis" text
);
CREATE TABLE "proxy_ip_info" (
 "id" text NOT NULL,
 "ip" text NOT NULL,
 "port" integer NOT NULL,
 "input_time" integer NOT NULL,
 "last_update_time" integer NOT NULL,
 "expiration_time" integer NOT NULL,
 "username" TEXT,
 "pwd" TEXT,
 "protocol_type" TEXT,
 PRIMARY KEY ("id")
);
